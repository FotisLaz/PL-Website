import React from "react";
import { render, screen } from "@testing-library/react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Layout from "./index";

// Mock Sidebar as we are unit testing Layout
jest.mock("../Sidebar", () => () => (
  <div data-testid="mocked-sidebar">Mocked Sidebar</div>
));

const TestChildComponent = () => (
  <div data-testid="test-child-content">Test Route Content</div>
);

describe("Layout Component", () => {
  test("renders the mocked Sidebar and Outlet content", () => {
    render(
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<TestChildComponent />} />
          </Route>
        </Routes>
      </BrowserRouter>
    );

    // Check if the mocked Sidebar is rendered
    expect(screen.getByTestId("mocked-sidebar")).toBeInTheDocument();

    // Check if the content from the Outlet (TestChildComponent) is rendered
    expect(screen.getByTestId("test-child-content")).toBeInTheDocument();
  });
});
