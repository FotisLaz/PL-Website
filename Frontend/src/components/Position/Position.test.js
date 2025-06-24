import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import Positions from "./index";

// Mock child components
jest.mock("../AnimatedLetters", () => ({ letterClass, strArray, idx }) => (
  <span data-testid="animated-letters">{strArray.join("")}</span>
));
jest.mock("react-loaders", () => () => (
  <div data-testid="loader">Mocked Loader</div>
));

// Mock the JSON import first with inline data
jest.mock("../../data/positions.json", () => ({
  positions: [
    { title: "Goalkeeper", cover: "gk.png", search: "GK" },
    { title: "Defender", cover: "def.png", search: "DF" },
    { title: "Midfielder", cover: "mid.png", search: "MF" },
    { title: "Forward", cover: "fwd.png", search: "FW" },
  ],
}));

// Define mock data for use in tests
const mockPositionData = {
  positions: [
    { title: "Goalkeeper", cover: "gk.png", search: "GK" },
    { title: "Defender", cover: "def.png", search: "DF" },
    { title: "Midfielder", cover: "mid.png", search: "MF" },
    { title: "Forward", cover: "fwd.png", search: "FW" },
  ],
};

describe("Positions Component", () => {
  const renderPositions = () =>
    render(
      <BrowserRouter>
        <Positions />
      </BrowserRouter>
    );

  test("renders page title and search bar", () => {
    renderPositions();
    expect(screen.getByText("Positions")).toBeInTheDocument();
    expect(
      screen.getByPlaceholderText("Search for positions")
    ).toBeInTheDocument();
  });

  test("renders all positions initially", () => {
    renderPositions();

    mockPositionData.positions.forEach((position) => {
      expect(screen.getByText(position.title)).toBeInTheDocument();
    });

    // Check for View links
    const viewLinks = screen.getAllByText(/View/i);
    expect(viewLinks).toHaveLength(mockPositionData.positions.length);
  });

  test("filters positions based on search query", () => {
    renderPositions();
    const searchInput = screen.getByPlaceholderText("Search for positions");

    fireEvent.change(searchInput, { target: { value: "Goalkeeper" } });
    expect(screen.getByText("Goalkeeper")).toBeInTheDocument();
    expect(screen.queryByText("Defender")).not.toBeInTheDocument();

    fireEvent.change(searchInput, { target: { value: "Forward" } });
    expect(screen.getByText("Forward")).toBeInTheDocument();
    expect(screen.queryByText("Midfielder")).not.toBeInTheDocument();
  });

  test("renders loader", () => {
    renderPositions();
    expect(screen.getByTestId("loader")).toBeInTheDocument();
  });
});
