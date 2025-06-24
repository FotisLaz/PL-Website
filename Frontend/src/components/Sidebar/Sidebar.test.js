import React from "react";
import { render, screen } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import Sidebar from "./index";

// Mock FontAwesomeIcon more safely
jest.mock("@fortawesome/react-fontawesome", () => ({
  FontAwesomeIcon: ({ icon, ...props }) => (
    <i data-testid={`fa-icon-${icon?.iconName || "unknown"}`} {...props} />
  ),
}));

// Mock image imports
jest.mock("../../assets/images/prem.PNG", () => "prem.PNG");
jest.mock("../../assets/images/sub-logo.png", () => "sub-logo.png");

describe("Sidebar Component", () => {
  const renderSidebar = () =>
    render(
      <BrowserRouter>
        <Sidebar />
      </BrowserRouter>
    );

  test("renders logo and sub-logo", () => {
    renderSidebar();
    expect(screen.getByAltText("logo")).toHaveAttribute("src", "prem.PNG");
    expect(screen.getByAltText("PremierZone")).toHaveAttribute(
      "src",
      "sub-logo.png"
    );
  });

  test("renders navigation links", () => {
    renderSidebar();

    // Test for navigation links by href since CSS content is not accessible
    expect(
      screen.getByRole("link", { name: "logo PremierZone" })
    ).toHaveAttribute("href", "/");

    // Check for navigation links by href
    const navLinks = screen.getAllByRole("link");
    const navHrefs = navLinks.map((link) => link.getAttribute("href"));

    expect(navHrefs).toContain("/");
    expect(navHrefs).toContain("/teams");
    expect(navHrefs).toContain("/nation");
    expect(navHrefs).toContain("/position");
    expect(navHrefs).toContain("/match-prediction");
    expect(navHrefs).toContain("/search");
  });

  test("renders FontAwesome icons", () => {
    renderSidebar();

    // Check that FontAwesome icons are rendered (adjust icon names as needed)
    const icons = screen.getAllByTestId(/^fa-icon-/);
    expect(icons.length).toBeGreaterThan(0);
  });
});
