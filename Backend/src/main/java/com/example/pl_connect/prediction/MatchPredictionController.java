package com.example.pl_connect.prediction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/match-prediction")
public class MatchPredictionController {

    private final PredictionService predictionService;

    @Autowired
    public MatchPredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping
    public ResponseEntity<?> predictMatch(@RequestBody MatchPredictionRequest request) {
        try {
            return ResponseEntity.ok(predictionService.getPrediction(request.getTeam1(), request.getTeam2()));
        } catch (InvalidTeamException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (PredictionUnavailableException e) {
            // PredictionService logs the details; the client gets a generic message
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Prediction is unavailable right now, please try again later"));
        }
    }
}
