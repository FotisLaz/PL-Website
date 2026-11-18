package com.example.pl_connect.prediction;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PredictionServiceTest {

    @Test
    void shouldThrowExceptionWhenPythonCannotBeStarted() {
        // Given
        PredictionService service = new PredictionService("no-such-python-executable", "prediction");

        // When & Then
        assertThatThrownBy(() -> service.getPrediction("Arsenal", "Chelsea"))
                .isInstanceOf(IOException.class);
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
