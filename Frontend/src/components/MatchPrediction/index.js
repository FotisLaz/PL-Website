import { useState } from "react";
import "./index.scss";

const MatchPrediction = () => {
  const [team1, setTeam1] = useState("");
  const [team2, setTeam2] = useState("");
  const [prediction, setPrediction] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handlePredict = async () => {
    if (!team1 || !team2) {
      setPrediction("Please select both teams.");
      return;
    }

    setLoading(true);
    setError(null);
    setPrediction(null);

    try {
      const response = await fetch(
        "http://localhost:8080/api/match-prediction",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            team1: team1,
            team2: team2,
          }),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();

      let resultMessage;
      if (data.Result === "Draw") {
        resultMessage = (
          <span className="result-draw">
            The match between {team1} and {team2} is predicted to end in a draw.
          </span>
        );
      } else {
        const probability = data.Probability
          ? (data.Probability * 100).toFixed(2)
          : "N/A";
        resultMessage = (
          <span className="result-win">
            {data.Result} with a probability of {probability}%.
          </span>
        );
      }
      setPrediction(resultMessage);
    } catch (err) {
      console.error(err);
      setError(
        "An error occurred while predicting the match. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="match-prediction">
      <h1>Match Prediction</h1>
      <div className="input-group">
        <label>
          Team 1:
          <input
            type="text"
            value={team1}
            onChange={(e) => setTeam1(e.target.value)}
            placeholder="Enter Team 1"
          />
        </label>
        <label>
          Team 2:
          <input
            type="text"
            value={team2}
            onChange={(e) => setTeam2(e.target.value)}
            placeholder="Enter Team 2"
          />
        </label>
      </div>
      <button onClick={handlePredict} disabled={loading}>
        {loading ? "Predicting..." : "Predict"}
      </button>
      {error && <p className="error-message">{error}</p>}
      {prediction && <p className="prediction-result">{prediction}</p>}
    </div>
  );
};

export default MatchPrediction;
