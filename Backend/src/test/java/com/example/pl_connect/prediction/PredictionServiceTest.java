package com.example.pl_connect.prediction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @InjectMocks
    private PredictionService predictionService;

    @BeforeEach
    void setUp() {
        // Mock the paths since we can't rely on PostConstruct in unit tests
        Path mockTempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path mockScriptPath = mockTempDir.resolve("pred.py");

        ReflectionTestUtils.setField(predictionService, "tempDir", mockTempDir);
        ReflectionTestUtils.setField(predictionService, "scriptPath", mockScriptPath);
    }

    @Test
    void shouldThrowExceptionWhenServiceNotInitialized() {
        // Given
        PredictionService uninitializedService = new PredictionService();

        // When & Then
        assertThatThrownBy(() -> uninitializedService.getPrediction("Arsenal", "Chelsea"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Prediction service resources not available");
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

    // Note: Testing the actual Python script execution would require integration
    // tests
    // since it depends on external Python installation and file system operations.
    // These tests focus on the service's state validation and basic functionality.
}