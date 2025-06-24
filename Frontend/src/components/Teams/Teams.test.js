import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import Teams from "./index";

// Mock child components
jest.mock("../AnimatedLetters", () => ({ letterClass, strArray, idx }) => (
  <span data-testid="animated-letters">{strArray.join("")}</span>
));
jest.mock("react-loaders", () => () => (
  <div data-testid="loader">Mocked Loader</div>
));

// Mock the JSON import first with inline data
jest.mock(
  "../../data/teams.json",
  () => ({
    teams: [
      { title: "Arsenal", cover: "arsenal.png", description: "Gunners" },
      { title: "Chelsea", cover: "chelsea.png", description: "Blues" },
      { title: "Liverpool", cover: "liverpool.png", description: "Reds" },
    ],
  }),
  { virtual: true }
);

// Define mock data for use in tests
const mockTeamData = {
  teams: [
    { title: "Arsenal", cover: "arsenal.png", description: "Gunners" },
    { title: "Chelsea", cover: "chelsea.png", description: "Blues" },
    { title: "Liverpool", cover: "liverpool.png", description: "Reds" },
  ],
};

describe("Teams Component", () => {
  const renderTeams = () =>
    render(
      <BrowserRouter>
        <Teams />
      </BrowserRouter>
    );

  test("renders page title and search bar", () => {
    renderTeams();
    expect(screen.getByText("Teams")).toBeInTheDocument(); // From mocked AnimatedLetters
    expect(screen.getByPlaceholderText("Search for teams")).toBeInTheDocument();
  });

  test("renders all teams initially", () => {
    renderTeams();
    mockTeamData.teams.forEach((team) => {
      expect(screen.getByText(team.title)).toBeInTheDocument();
      const images = screen.getAllByAltText("teams");
      const teamImage = images.find(
        (img) => img.getAttribute("src") === team.cover
      );
      expect(teamImage).toBeInTheDocument();
      expect(teamImage).toHaveAttribute("src", team.cover);
    });

    const viewLinks = screen.getAllByRole("link", { name: /View/i });
    expect(viewLinks).toHaveLength(mockTeamData.teams.length);
  });

  test("filters teams based on search query", () => {
    renderTeams();
    const searchInput = screen.getByPlaceholderText("Search for teams");

    fireEvent.change(searchInput, { target: { value: "Arsenal" } });
    expect(screen.getByText("Arsenal")).toBeInTheDocument();
    expect(screen.queryByText("Chelsea")).not.toBeInTheDocument();
    expect(screen.queryByText("Liverpool")).not.toBeInTheDocument();

    fireEvent.change(searchInput, { target: { value: "li" } });
    expect(screen.getByText("Liverpool")).toBeInTheDocument();
    expect(screen.queryByText("Arsenal")).not.toBeInTheDocument();
    expect(screen.queryByText("Chelsea")).not.toBeInTheDocument();
  });

  test("renders loader", () => {
    renderTeams();
    expect(screen.getByTestId("loader")).toBeInTheDocument();
  });
});
