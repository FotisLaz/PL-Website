import { render, screen, waitFor } from "@testing-library/react";
import axios from "axios";
import DataHandling from "./index";

jest.mock("axios");
const mockGet = jest.spyOn(axios, "get");

let mockURLSearchParams;
beforeEach(() => {
  mockURLSearchParams = {
    get: jest.fn(),
  };
  jest
    .spyOn(window, "URLSearchParams")
    .mockImplementation(() => mockURLSearchParams);
});

afterEach(() => {
  jest.restoreAllMocks();
});

describe("DataHandling Component", () => {
  test("does not fetch data if no team parameter is present", async () => {
    mockURLSearchParams.get.mockReturnValue(null);

    render(<DataHandling />);

    expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
    expect(axios.get).not.toHaveBeenCalled();
  });

  // Test case: shows loading state initially
  test("shows loading state initially", async () => {
    mockURLSearchParams.get.mockReturnValue("TeamA"); // Έχουμε παράμετρο team

    mockGet.mockImplementation(() => new Promise(() => {}));

    render(<DataHandling />);

    expect(screen.getByText("Loading...")).toBeInTheDocument();
  });

  // Test case: renders player data on successful fetch
  test("renders player data on successful fetch", async () => {
    mockURLSearchParams.get.mockReturnValue("TeamB"); // Έχουμε παράμετρο team
    const mockPlayers = [
      {
        name: "Player 1",
        team: "TeamB",
        pos: "FW",
        age: 25,
        mp: 10,
        starts: 8,
        min: 700,
        gls: 5,
        ast: 3,
        pk: 1,
        crdy: 2,
        crdr: 0,
        xg: 4.5,
        xag: 2.5,
      },
    ];

    mockGet.mockResolvedValue({ data: mockPlayers }); // Mock επιτυχής απάντηση

    render(<DataHandling />);

    // Περίμενε να εξαφανιστεί το loading state και να εμφανιστούν τα δεδομένα
    await waitFor(() =>
      expect(screen.queryByText("Loading...")).not.toBeInTheDocument()
    );
    expect(screen.getByText("Player 1")).toBeInTheDocument();
    expect(axios.get).toHaveBeenCalledWith(
      `http://localhost:8080/api/v1/player?team=TeamB`
    );
  });

  // Test case: shows error message on fetch failure
  test("shows error message on fetch failure", async () => {
    mockURLSearchParams.get.mockReturnValue("TeamC"); // Έχουμε παράμετρο team
    const networkErrorMessage = "Network Error";
    mockGet.mockRejectedValue(new Error(networkErrorMessage)); // Mock αποτυχημένη απάντηση

    render(<DataHandling />);

    await waitFor(() =>
      expect(screen.queryByText("Loading...")).not.toBeInTheDocument()
    );
    expect(
      screen.getByText(`Error: ${networkErrorMessage}`)
    ).toBeInTheDocument();
    expect(axios.get).toHaveBeenCalledWith(
      `http://localhost:8080/api/v1/player?team=TeamC`
    );
  });
});
