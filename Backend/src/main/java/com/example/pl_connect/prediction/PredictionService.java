package com.example.pl_connect.prediction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PredictionService {

    private static final Logger log = LoggerFactory.getLogger(PredictionService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // python + predict.py; the model it loads is trained when the Docker image is built
    private final List<String> command;
    private final long timeoutSeconds;

    @Autowired
    public PredictionService(@Value("${prediction.python}") String python,
                             @Value("${prediction.dir}") String predictionDir,
                             @Value("${prediction.timeout-seconds}") long timeoutSeconds) {
        this(List.of(python, Path.of(predictionDir, "predict.py").toString()), timeoutSeconds);
    }

    // Tests pass another command here, so they can run without Python
    PredictionService(List<String> command, long timeoutSeconds) {
        this.command = List.copyOf(command);
        this.timeoutSeconds = timeoutSeconds;
    }

    public JsonNode getPrediction(String team1, String team2) {
        List<String> fullCommand = new ArrayList<>(command);
        fullCommand.add(team1);
        fullCommand.add(team2);

        Path output = null;
        Process process = null;
        try {
            // stdout goes to a file, so this thread never blocks on a read: it only waits for
            // the process to exit, up to the timeout. stderr goes to the application log.
            output = Files.createTempFile("prediction-", ".json");
            process = new ProcessBuilder(fullCommand)
                    .redirectOutput(output.toFile())
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start();

            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                throw unavailable("Prediction timed out after " + timeoutSeconds + " s: " + fullCommand, null);
            }
            String stdout = Files.readString(output).trim();
            if (process.exitValue() != 0) {
                throw unavailable("Prediction exited with code " + process.exitValue() + ": " + fullCommand, null);
            }
            JsonNode result = MAPPER.readTree(stdout);
            if (!result.path("Result").isTextual() || !result.path("Probability").isNumber()) {
                throw unavailable("Prediction returned unexpected output: " + stdout, null);
            }
            log.info("Prediction {} vs {}: {}", team1, team2, stdout);
            return result;
        } catch (IOException e) {
            throw unavailable("Prediction failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw unavailable("Prediction was interrupted", e);
        } finally {
            if (process != null && process.isAlive()) {
                // Timed out or interrupted: kill Python instead of leaving it running
                process.destroyForcibly();
                try {
                    process.waitFor(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (output != null) {
                try {
                    Files.deleteIfExists(output);
                } catch (IOException e) {
                    log.warn("Could not delete {}", output, e);
                }
            }
        }
    }

    private static PredictionUnavailableException unavailable(String reason, Exception cause) {
        log.error(reason, cause);
        return new PredictionUnavailableException(reason, cause);
    }
}
