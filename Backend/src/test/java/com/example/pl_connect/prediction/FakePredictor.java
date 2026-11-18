package com.example.pl_connect.prediction;

/**
 * Stands in for predict.py in PredictionServiceTest, so the tests run without Python.
 * It runs in its own JVM; the first argument picks the behaviour, then come the two teams.
 */
public class FakePredictor {

    public static void main(String[] args) throws InterruptedException {
        String mode = args[0];
        switch (mode) {
            case "ok" -> System.out.println("{\"Result\": \"" + args[1] + " Wins\", \"Probability\": 0.6}");
            case "hang" -> {
                // Some output, then no exit: the service must not wait on the output stream
                System.out.print("{\"Result\": ");
                System.out.flush();
                Thread.sleep(60_000);
            }
            case "crash" -> {
                System.err.println("FakePredictor: simulated failure");
                System.exit(1);
            }
            case "garbage" -> System.out.println("not json");
            default -> throw new IllegalArgumentException("Unknown mode: " + mode);
        }
    }
}
