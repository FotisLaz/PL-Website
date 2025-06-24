import React from "react";
import { render, screen } from "@testing-library/react";
import AnimatedLetters from "./index"; // Points to index.js in the current folder

describe("AnimatedLetters Component", () => {
  test("renders each character of strArray in a span", () => {
    const testString = "Hello";
    const strArray = testString.split("");
    render(
      <AnimatedLetters letterClass="test-class" strArray={strArray} idx={1} />
    );

    strArray.forEach((char, index) => {
      const elements = screen.getAllByText(char, { exact: false });
      expect(elements.length).toBeGreaterThanOrEqual(1);
    });

    // Specifically for 'l' which appears twice in "Hello"
    const lElements = screen.getAllByText("l", { exact: false });
    expect(lElements).toHaveLength(2);

    const hElement = screen.getByText("H", { exact: false });
    expect(hElement).toBeInTheDocument();
  });

  test("applies the correct letterClass and idx to spans", () => {
    const strArray = ["H", "i"];
    const letterClass = "custom-letter";
    const idx = 5;
    render(
      <AnimatedLetters
        letterClass={letterClass}
        strArray={strArray}
        idx={idx}
      />
    );

    const firstLetterSpan = screen.getByText(strArray[0], { exact: false });
    expect(firstLetterSpan).toHaveClass(letterClass);
    expect(firstLetterSpan).toHaveClass(`_${0 + idx}`); // Check for the class like '_5'

    const secondLetterSpan = screen.getByText(strArray[1], { exact: false });
    expect(secondLetterSpan).toHaveClass(letterClass);
    expect(secondLetterSpan).toHaveClass(`_${1 + idx}`); // Check for the class like '_6'
  });
});
