package com.example.pl_connect.prediction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import jakarta.annotation.PostConstruct;

@Service
public class PredictionService {

    private static final Logger log = LoggerFactory.getLogger(PredictionService.class);
    private Path scriptPath;
    private Path tempDir;

    @PostConstruct
    public void init() {
        try {
            // Extract Python script and CSV to a temp directory
            tempDir = Files.createTempDirectory("pl-predictor-");
            log.info("Created temporary directory for prediction scripts: {}", tempDir);

            // Copy the prediction script
            ClassPathResource predScriptResource = new ClassPathResource("python/pred.py");
            if (!predScriptResource.exists()) {
                log.error("Prediction script 'python/pred.py' not found in resources!");
                throw new IOException("Missing prediction script resource");
            }
            scriptPath = tempDir.resolve("pred.py");
            Files.copy(predScriptResource.getInputStream(), scriptPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Copied prediction script to: {}", scriptPath);

            // Copy the matches CSV
            ClassPathResource matchesResource = new ClassPathResource("python/matches.csv");
             if (!matchesResource.exists()) {
                log.error("Matches CSV 'python/matches.csv' not found in resources!");
                // Decide if this is critical - maybe prediction can run without it?
                // For now, let's throw an error or handle appropriately.
                 throw new IOException("Missing matches CSV resource");
            }
            Path matchesPath = tempDir.resolve("matches.csv");
            Files.copy(matchesResource.getInputStream(), matchesPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Copied matches CSV to: {}", matchesPath);

        } catch (IOException e) {
            log.error("Failed to initialize PredictionService resources: {}", e.getMessage(), e);
            // Handle the error appropriately - maybe disable prediction functionality?
            // Rethrowing as RuntimeException to prevent application startup if critical
            throw new RuntimeException("Failed to initialize prediction resources", e);
        }
    }

    public String getPrediction(String team1, String team2) throws Exception {
        if (scriptPath == null || tempDir == null) {
             log.error("Prediction service not initialized correctly. Cannot get prediction.");
            throw new IllegalStateException("Prediction service resources not available.");
        }

        ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPath.toAbsolutePath().toString(), team1, team2);
        processBuilder.redirectErrorStream(true);
        processBuilder.directory(tempDir.toFile()); // Set the working directory!
        log.info("Executing prediction script: python {} {} {} in directory {}", scriptPath.getFileName(), team1, team2, tempDir);

        Process process = processBuilder.start();

        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        log.info("Prediction script finished with exit code: {}", exitCode);

        if (exitCode != 0) {
            log.error("Python script execution failed. Output:\n{}", result.toString().trim());
            throw new Exception("Python script exited with code " + exitCode + ". Output: " + result.toString().trim());
        }

         String predictionResult = result.toString().trim();
        log.info("Prediction result: {}", predictionResult);
        return predictionResult;
    }
}
