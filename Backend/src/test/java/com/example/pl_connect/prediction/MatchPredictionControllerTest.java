package com.example.pl_connect.prediction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchPredictionController.class)
class MatchPredictionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PredictionService predictionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnPredictionWhenValidRequest() throws Exception {
        // Given
        MatchPredictionRequest request = new MatchPredictionRequest("Arsenal", "Chelsea");
        String expectedPrediction = "Arsenal is likely to win with 65% probability";

        when(predictionService.getPrediction("Arsenal", "Chelsea"))
                .thenReturn(expectedPrediction);

        // When & Then
        mockMvc.perform(post("/api/match-prediction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedPrediction));
    }

    @Test
    void shouldReturnErrorWhenPredictionServiceThrowsException() throws Exception {
        // Given
        MatchPredictionRequest request = new MatchPredictionRequest("Arsenal", "Chelsea");

        when(predictionService.getPrediction("Arsenal", "Chelsea"))
                .thenThrow(new RuntimeException("Python script not found"));

        // When & Then
        mockMvc.perform(post("/api/match-prediction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error: Python script not found"));
    }

    @Test
    void shouldReturnPredictionForDifferentTeams() throws Exception {
        // Given
        MatchPredictionRequest request = new MatchPredictionRequest("Liverpool", "Manchester City");
        String expectedPrediction = "Draw is most likely with 40% probability";

        when(predictionService.getPrediction("Liverpool", "Manchester City"))
                .thenReturn(expectedPrediction);

        // When & Then
        mockMvc.perform(post("/api/match-prediction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedPrediction));
    }
}