import { render, screen } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import App from "./App";

// Mock child components to simplify App testing
jest.mock("./components/AnimatedLetters", () => () => (
  <span data-testid="animated-letters">Mocked AnimatedLetters</span>
));
jest.mock("react-loaders", () => () => (
  <div data-testid="loader">Mocked Loader</div>
));

test("renders Welcome to PremierZone! and Get Started button", () => {
  render(
    <BrowserRouter>
      <App />
    </BrowserRouter>
  );

  // Check for the mocked AnimatedLetters component (assuming it's part of the welcome message)
  // This assertion depends on Home component rendering AnimatedLetters for "Welcome to" and "PremierZone!"
  // If Home itself changes how it uses AnimatedLetters, this test might need adjustment.
  expect(
    screen.getAllByTestId("animated-letters").length
  ).toBeGreaterThanOrEqual(1);

  // Check for the "GET STARTED" button
  expect(
    screen.getByRole("link", { name: /GET STARTED/i })
  ).toBeInTheDocument();
});
