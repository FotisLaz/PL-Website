import React from "react";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import axios from "axios";
import TeamData from "./index";

// Mock axios and child components
jest.mock("axios");
const mockedAxios = axios;

jest.mock("../AnimatedLetters", () => ({ letterClass, strArray, idx }) => (
  <span data-testid="animated-letters">{strArray.join("")}</span>
));

describe("TeamData Component", () => {
  let originalLocation;

  beforeEach(() => {
    mockedAxios.get.mockReset();

    // Mock window.location
    originalLocation = window.location;
    delete window.location;
    window.location = { ...originalLocation, search: "" };
  });

  afterEach(() => {
    window.location = originalLocation;
  });

  const mockPlayerData = [
    {
      name: "Player Alpha",
      pos: "FW",
      age: 22,
      mp: 5,
      starts: 5,
      min: 450,
      gls: 3,
      ast: 1,
      pk: 0,
      crdy: 0,
      crdr: 0,
      xg: 2.5,
      xag: 0.8,
      team: "Team X",
    },
    {
      name: "Player Beta",
      pos: "MF",
      age: 26,
      mp: 8,
      starts: 7,
      min: 600,
      gls: 1,
      ast: 4,
      pk: 1,
      crdy: 1,
      crdr: 0,
      xg: 1.5,
      xag: 3.5,
      team: "Team X",
    },
  ];

  test("shows loading state then renders data for team parameter", async () => {
    window.location.search = "?team=TeamX";
    mockedAxios.get.mockResolvedValueOnce({ data: mockPlayerData });

    render(<TeamData />);

    expect(screen.getByText("Loading...")).toBeInTheDocument();

    await waitFor(() =>
      expect(screen.queryByText("Loading...")).not.toBeInTheDocument()
    );

    expect(screen.getByText("Player Alpha")).toBeInTheDocument();
    expect(mockedAxios.get).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/player?team=TeamX"
    );
  });

  test("shows loading state then renders data for nation parameter", async () => {
    window.location.search = "?nation=NationY";
    mockedAxios.get.mockResolvedValueOnce({ data: mockPlayerData });

    render(<TeamData />);

    expect(screen.getByText("Loading...")).toBeInTheDocument();

    await waitFor(() =>
      expect(screen.queryByText("Loading...")).not.toBeInTheDocument()
    );

    expect(screen.getByText("Player Beta")).toBeInTheDocument();
    expect(mockedAxios.get).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/player?nation=NationY"
    );
  });

  test("shows loading state then renders data for position parameter", async () => {
    window.location.search = "?position=Defender";
    mockedAxios.get.mockResolvedValueOnce({ data: mockPlayerData });

    render(<TeamData />);

    expect(screen.getByText("Loading...")).toBeInTheDocument();

    await waitFor(() =>
      expect(screen.queryByText("Loading...")).not.toBeInTheDocument()
    );

    expect(screen.getByText("Player Alpha")).toBeInTheDocument();
    expect(mockedAxios.get).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/player?position=Defender"
    );
  });

  test("shows loading state then renders data for name parameter", async () => {
    window.location.search = "?name=PlayerAlpha";
    mockedAxios.get.mockResolvedValueOnce({ data: mockPlayerData });

    render(<TeamData />);

    expect(screen.getByText("Loading...")).toBeInTheDocument();

    await waitFor(() =>
      expect(screen.queryByText("Loading...")).not.toBeInTheDocument()
    );

    expect(screen.getByText("Player Alpha")).toBeInTheDocument();
    expect(mockedAxios.get).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/player?name=PlayerAlpha"
    );
  });

  test("shows error on API failure", async () => {
    window.location.search = "?team=TeamZ";
    const errorMessage = "Fetch Error";
    mockedAxios.get.mockRejectedValueOnce(new Error(errorMessage));

    render(<TeamData />);

    await waitFor(() =>
      expect(screen.queryByText("Loading...")).not.toBeInTheDocument()
    );

    expect(screen.getByText(`Error: ${errorMessage}`)).toBeInTheDocument();
  });

  test("does not show loading or error if no relevant params are present", () => {
    window.location.search = "";

    render(<TeamData />);

    expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
    expect(screen.queryByText(/Error:/i)).not.toBeInTheDocument();
    expect(mockedAxios.get).not.toHaveBeenCalled();
  });

  test("Show More button appears and loads more players", async () => {
    const morePlayerData = Array.from({ length: 15 }, (_, i) => ({
      name: `Player ${i + 1}`,
      pos: "FW",
      age: 20 + i,
      mp: 10,
      starts: 5,
      min: 500,
      gls: i,
      ast: i,
      pk: 0,
      crdy: 0,
      crdr: 0,
      xg: i,
      xag: i,
      team: "ManyPlayersTeam",
    }));

    window.location.search = "?team=ManyPlayersTeam";
    mockedAxios.get.mockResolvedValueOnce({ data: morePlayerData });

    render(<TeamData />);

    await waitFor(() =>
      expect(screen.queryByText("Loading...")).not.toBeInTheDocument()
    );

    // Initially 10 players should be visible - check table rows instead of text
    const tableRows = screen.getAllByRole("row");
    // 1 header row + 10 player rows = 11 total rows
    expect(tableRows.length).toBe(11);

    const showMoreButton = screen.getByRole("button", { name: /Show More/i });
    expect(showMoreButton).toBeInTheDocument();

    fireEvent.click(showMoreButton);

    // After click, more players should be visible - 1 header row + 15 player rows = 16 total rows
    await waitFor(() => {
      const updatedTableRows = screen.getAllByRole("row");
      expect(updatedTableRows.length).toBe(16);
    });

    expect(
      screen.queryByRole("button", { name: /Show More/i })
    ).not.toBeInTheDocument();
  });
});
