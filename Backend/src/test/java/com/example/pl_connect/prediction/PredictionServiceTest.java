package com.example.pl_connect.prediction;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PredictionServiceTest {

    // Runs FakePredictor in a new JVM instead of python predict.py
    private static PredictionService fakeService(String mode, long timeoutSeconds) throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classes = Path.of(FakePredictor.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .toString();
        return new PredictionService(List.of(java, "-cp", classes, FakePredictor.class.getName(), mode),
                timeoutSeconds);
    }

    private static Set<Long> liveChildPids() {
        return ProcessHandle.current().children()
                .filter(ProcessHandle::isAlive)
                .map(ProcessHandle::pid)
                .collect(Collectors.toSet());
    }

    @Test
    void shouldReturnThePredictionJson() throws Exception {
        // When
        JsonNode result = fakeService("ok", 30).getPrediction("Arsenal", "Chelsea");

        // Then
        assertThat(result.get("Result").asText()).isEqualTo("Arsenal Wins");
        assertThat(result.get("Probability").asDouble()).isEqualTo(0.6);
    }

    @Test
    void shouldKillThePredictionProcessWhenItTimesOut() throws Exception {
        // Given
        PredictionService service = fakeService("hang", 1);
        Set<Long> childrenBefore = liveChildPids();
        long start = System.nanoTime();

        // When & Then: FakePredictor sleeps 60 s, the service gives up after 1 s
        assertThatThrownBy(() -> service.getPrediction("Arsenal", "Chelsea"))
                .isInstanceOf(PredictionUnavailableException.class)
                .hasMessageContaining("timed out");
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(15));
        // The process was killed, not left running
        assertThat(liveChildPids()).isSubsetOf(childrenBefore);
    }

    @Test
    void shouldFailWhenThePredictionProcessExitsWithAnError() throws Exception {
        assertThatThrownBy(() -> fakeService("crash", 30).getPrediction("Arsenal", "Chelsea"))
                .isInstanceOf(PredictionUnavailableException.class)
                .hasMessageContaining("exited with code 1");
    }

    @Test
    void shouldFailWhenThePredictionOutputIsNotJson() throws Exception {
        assertThatThrownBy(() -> fakeService("garbage", 30).getPrediction("Arsenal", "Chelsea"))
                .isInstanceOf(PredictionUnavailableException.class);
    }

    @Test
    void shouldFailWhenPythonCannotBeStarted() {
        // Given
        PredictionService service = new PredictionService("no-such-python-executable", "prediction", 10);

        // When & Then
        assertThatThrownBy(() -> service.getPrediction("Arsenal", "Chelsea"))
                .isInstanceOf(PredictionUnavailableException.class);
    }

    @Test
    void shouldConstructMatchPredictionRequest() {
        // When
        MatchPredictionRequest request = new MatchPredictionRequest("Arsenal", "Chelsea");

        // Then
        assertThat(request.getTeam1()).isEqualTo("Arsenal");
        assertThat(request.getTeam2()).isEqualTo("Chelsea");
    }

    @Test
    void shouldSetTeamsInMatchPredictionRequest() {
        // Given
        MatchPredictionRequest request = new MatchPredictionRequest();

        // When
        request.setTeam1("Liverpool");
        request.setTeam2("Manchester City");

        // Then
        assertThat(request.getTeam1()).isEqualTo("Liverpool");
        assertThat(request.getTeam2()).isEqualTo("Manchester City");
    }
}
