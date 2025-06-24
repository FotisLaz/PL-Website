import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import Search from "./index";

// Mock child components
jest.mock("../AnimatedLetters", () => ({ letterClass, strArray, idx }) => (
  <span data-testid="animated-letters">{strArray.join("")}</span>
));
jest.mock("react-loaders", () => () => (
  <div data-testid="loader">Mocked Loader</div>
));

// Mock window.location
let originalLocation;

describe("Search Component", () => {
  beforeEach(() => {
    originalLocation = window.location;
    delete window.location;
    window.location = { 
      ...originalLocation, 
      href: "" // Make it a string property that can be assigned to
    };
  });

  afterEach(() => {
    window.location = originalLocation;
  });

  const renderSearch = () => render(<Search />);

  test("renders page title, search input, and go button", () => {
    renderSearch();
    expect(screen.getByText("Search")).toBeInTheDocument(); // From mocked AnimatedLetters
    expect(
      screen.getByPlaceholderText("Search for players")
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Go/i })).toBeInTheDocument();
  });

  test("updates search query on input change", () => {
    renderSearch();
    const searchInput = screen.getByPlaceholderText("Search for players");
    fireEvent.change(searchInput, { target: { value: "Kane" } });
    expect(searchInput.value).toBe("Kane");
  });

  test("navigates to correct URL on Go button click", () => {
    renderSearch();
    const searchInput = screen.getByPlaceholderText("Search for players");
    const goButton = screen.getByRole("button", { name: /Go/i });
    const searchQuery = "Haaland";

    fireEvent.change(searchInput, { target: { value: searchQuery } });
    fireEvent.click(goButton);

    // Check that window.location.href was set to the expected URL
    expect(window.location.href).toBe(
      `/data?name=${encodeURIComponent(searchQuery)}`
    );
  });

  test("renders loader", () => {
    renderSearch();
    expect(screen.getByTestId("loader")).toBeInTheDocument();
  });
});