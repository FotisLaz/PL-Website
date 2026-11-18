package com.example.pl_connect.prediction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class PredictionService {

    private static final Logger log = LoggerFactory.getLogger(PredictionService.class);

    // python + predict.py; the model it loads is trained when the Docker image is built
    private final List<String> command;

    public PredictionService(@Value("${prediction.python}") String python,
                             @Value("${prediction.dir}") String predictionDir) {
        this.command = List.of(python, Path.of(predictionDir, "predict.py").toString());
    }

    public String getPrediction(String team1, String team2) throws Exception {
        List<String> fullCommand = new ArrayList<>(command);
        fullCommand.add(team1);
        fullCommand.add(team2);

        ProcessBuilder processBuilder = new ProcessBuilder(fullCommand);
        processBuilder.redirectErrorStream(true);
        log.info("Executing prediction: {}", fullCommand);

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
