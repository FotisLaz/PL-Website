package com.example.pl_connect.prediction;

/** The prediction could not be made: the model failed, timed out or could not be started. HTTP 503. */
public class PredictionUnavailableException extends RuntimeException {

    public PredictionUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
