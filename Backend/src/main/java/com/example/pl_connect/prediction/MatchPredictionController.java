package com.example.pl_connect.prediction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            String result = predictionService.getPrediction(request.getTeam1(), request.getTeam2());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
