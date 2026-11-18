package com.example.pl_connect.prediction;

/** A team name is missing or unknown, or both names are the same team. HTTP 400. */
public class InvalidTeamException extends RuntimeException {

    public InvalidTeamException(String message) {
        super(message);
    }
}
