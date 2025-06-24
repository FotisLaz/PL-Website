import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import Nations from "./index";

// Mock child components
jest.mock("../AnimatedLetters", () => ({ letterClass, strArray, idx }) => (
  <span data-testid="animated-letters">{strArray.join("")}</span>
));

jest.mock("react-loaders", () => () => (
  <div data-testid="loader">Mocked Loader</div>
));

jest.mock("react-country-flag", () => ({
  ReactCountryFlag: ({ countryCode, svg, style }) => (
    <svg data-testid={`country-flag-${countryCode}`} style={style} />
  ),
}));

// Mock the JSON import first with inline data
jest.mock("../../data/nations.json", () => ({
  nations: [
    { name: "England", code: "GB-ENG", search: "England" },
    { name: "Brazil", code: "BR", search: "Brazil" },
    { name: "Spain", code: "ES", search: "Spain" },
  ],
}));

// Define mock data for use in tests
const mockNationsData = {
  nations: [
    { name: "England", code: "GB-ENG", search: "England" },
    { name: "Brazil", code: "BR", search: "Brazil" },
    { name: "Spain", code: "ES", search: "Spain" },
  ],
};

describe("Nations Component", () => {
  const renderNations = () =>
    render(
      <BrowserRouter>
        <Nations />
      </BrowserRouter>
    );

  beforeEach(() => {
    // Clear any previous renders
    jest.clearAllMocks();
  });

  test("renders page title and search bar", () => {
    renderNations();
    expect(screen.getByText("Nations")).toBeInTheDocument();
    expect(
      screen.getByPlaceholderText("Search for countries")
    ).toBeInTheDocument();
  });

  test("renders all nations initially with flags", () => {
    renderNations();

    mockNationsData.nations.forEach((nation) => {
      expect(screen.getByText(nation.name)).toBeInTheDocument();
      expect(
        screen.getByTestId(`country-flag-${nation.code}`)
      ).toBeInTheDocument();
    });

    // Check for View links (simplified assertion)
    const viewLinks = screen.getAllByText(/View/i);
    expect(viewLinks).toHaveLength(mockNationsData.nations.length);
  });

  test("filters nations based on search query", () => {
    renderNations();
    const searchInput = screen.getByPlaceholderText("Search for countries");

    fireEvent.change(searchInput, { target: { value: "England" } });
    expect(screen.getByText("England")).toBeInTheDocument();
    expect(screen.queryByText("Brazil")).not.toBeInTheDocument();

    fireEvent.change(searchInput, { target: { value: "spa" } });
    expect(screen.getByText("Spain")).toBeInTheDocument();
    expect(screen.queryByText("England")).not.toBeInTheDocument();
  });

  test("renders loader", () => {
    renderNations();
    expect(screen.getByTestId("loader")).toBeInTheDocument();
  });
});
