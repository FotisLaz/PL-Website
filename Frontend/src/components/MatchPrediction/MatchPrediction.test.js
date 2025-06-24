import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import MatchPrediction from "./index";

// Mock the fetch API globally
global.fetch = jest.fn();

describe("MatchPrediction Component", () => {
  beforeEach(() => {
    fetch.mockClear(); // Clear mock usage before each test
  });

  test("renders input fields and predict button", () => {
    render(<MatchPrediction />);
    expect(screen.getByPlaceholderText("Enter Team 1")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Enter Team 2")).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /Predict/i })
    ).toBeInTheDocument();
  });

  test("shows a message if teams are not selected on predict click", () => {
    render(<MatchPrediction />);
    fireEvent.click(screen.getByRole("button", { name: /Predict/i }));
    expect(screen.getByText("Please select both teams.")).toBeInTheDocument();
  });

  test("calls API and displays prediction for a win", async () => {
    const team1 = "Arsenal";
    const team2 = "Chelsea";
    const mockPrediction = { Result: "Arsenal", Probability: 0.75 };
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockPrediction,
    });

    render(<MatchPrediction />);

    fireEvent.change(screen.getByPlaceholderText("Enter Team 1"), {
      target: { value: team1 },
    });
    fireEvent.change(screen.getByPlaceholderText("Enter Team 2"), {
      target: { value: team2 },
    });
    fireEvent.click(screen.getByRole("button", { name: /Predict/i }));

    expect(
      screen.getByRole("button", { name: /Predicting.../i })
    ).toBeInTheDocument();

    await waitFor(() => {
      expect(
        screen.getByText(
          `${mockPrediction.Result} with a probability of ${(
            mockPrediction.Probability * 100
          ).toFixed(2)}%.`
        )
      ).toBeInTheDocument();
    });

    expect(fetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/match-prediction",
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ team1, team2 }),
      }
    );
  });

  test("calls API and displays prediction for a draw", async () => {
    const team1 = "Liverpool";
    const team2 = "Man City";
    const mockPrediction = { Result: "Draw" };
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockPrediction,
    });

    render(<MatchPrediction />);
    fireEvent.change(screen.getByPlaceholderText("Enter Team 1"), {
      target: { value: team1 },
    });
    fireEvent.change(screen.getByPlaceholderText("Enter Team 2"), {
      target: { value: team2 },
    });
    fireEvent.click(screen.getByRole("button", { name: /Predict/i }));

    await waitFor(() => {
      expect(
        screen.getByText(
          `The match between ${team1} and ${team2} is predicted to end in a draw.`
        )
      ).toBeInTheDocument();
    });
  });

  test("displays error message on API failure", async () => {
    fetch.mockResolvedValueOnce({
      ok: false,
      status: 500,
    });

    render(<MatchPrediction />);
    fireEvent.change(screen.getByPlaceholderText("Enter Team 1"), {
      target: { value: "Spurs" },
    });
    fireEvent.change(screen.getByPlaceholderText("Enter Team 2"), {
      target: { value: "Everton" },
    });
    fireEvent.click(screen.getByRole("button", { name: /Predict/i }));

    await waitFor(() => {
      expect(
        screen.getByText(
          "An error occurred while predicting the match. Please try again."
        )
      ).toBeInTheDocument();
    });
  });
});
