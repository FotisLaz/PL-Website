import React from "react";
import { render, screen } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom"; // Needed because Home uses <Link>
import Home from "./index";

// Mock AnimatedLetters to simplify Home component testing
jest.mock("../AnimatedLetters", () => () => (
  <span data-testid="animated-letters">Mocked AnimatedLetters</span>
));
// Mock Loader to prevent issues if it has complex side effects or rendering
jest.mock("react-loaders", () => () => (
  <div data-testid="loader">Mocked Loader</div>
));

describe("Home Component", () => {
  const renderHome = () =>
    render(
      <BrowserRouter>
        <Home />
      </BrowserRouter>
    );

  test("renders welcome text and Premier StatZone text", () => {
    renderHome();
    // Check for parts of the animated text.

    expect(
      screen.getAllByTestId("animated-letters").length
    ).toBeGreaterThanOrEqual(1);
  });

  test("renders the main heading image", () => {
    renderHome();
    const logoImage = screen.getByAltText("PremierZone");
    expect(logoImage).toBeInTheDocument();
    expect(logoImage).toHaveAttribute("src", "prem.PNG"); // Assuming LogoPL is imported as prem.PNG
  });

  test("renders the subtitle", () => {
    renderHome();
    expect(
      screen.getByText("Your home for everything Premier League related!")
    ).toBeInTheDocument();
  });

  test("renders the GET STARTED link button", () => {
    renderHome();
    const linkButton = screen.getByRole("link", { name: /get started/i });
    expect(linkButton).toBeInTheDocument();
    expect(linkButton).toHaveAttribute("href", "/teams");
  });

  // Test for Loader visibility if needed, though mocking might be simpler.
  test("renders the Loader component", () => {
    renderHome();
    expect(screen.getByTestId("loader")).toBeInTheDocument(); // If you mocked it with data-testid
  });
});
