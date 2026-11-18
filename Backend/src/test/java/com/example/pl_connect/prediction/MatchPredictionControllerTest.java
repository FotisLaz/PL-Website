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

        when(predictionService.getPrediction("Arsenal", "Chelsea"))
                .thenReturn(objectMapper.readTree("{\"Result\": \"Arsenal Wins\", \"Probability\": 0.65}"));

        // When & Then
        mockMvc.perform(post("/api/match-prediction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.Result").value("Arsenal Wins"))
                .andExpect(jsonPath("$.Probability").value(0.65));
    }

    @Test
    void shouldReturn400ForAnUnknownTeam() throws Exception {
        // Given
        MatchPredictionRequest request = new MatchPredictionRequest("Arsnal", "Chelsea");

        when(predictionService.getPrediction("Arsnal", "Chelsea"))
                .thenThrow(new InvalidTeamException("Unknown team: Arsnal"));

        // When & Then
        mockMvc.perform(post("/api/match-prediction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown team: Arsnal"));
    }

    @Test
    void shouldReturn503WhenPredictionIsUnavailable() throws Exception {
        // Given
        MatchPredictionRequest request = new MatchPredictionRequest("Arsenal", "Chelsea");

        when(predictionService.getPrediction("Arsenal", "Chelsea"))
                .thenThrow(new PredictionUnavailableException("Prediction timed out after 10 s", null));

        // When & Then: a generic message, not the internal one
        mockMvc.perform(post("/api/match-prediction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Prediction is unavailable right now, please try again later"));
    }

    @Test
    void shouldReturnPredictionForDifferentTeams() throws Exception {
        // Given
        MatchPredictionRequest request = new MatchPredictionRequest("Liverpool", "Manchester City");

        when(predictionService.getPrediction("Liverpool", "Manchester City"))
                .thenReturn(objectMapper.readTree("{\"Result\": \"Draw\", \"Probability\": 0.5}"));

        // When & Then
        mockMvc.perform(post("/api/match-prediction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Result").value("Draw"));
    }
}
