"""Predicts a match with the model saved by train.py (no training here).

Usage: python predict.py <team1> <team2>
Prints {"Result": ..., "Probability": ...} as JSON on stdout. Called by PredictionService.
"""
import json
import sys
from pathlib import Path

import pandas as pd
from xgboost import XGBClassifier

MODEL_DIR = Path(__file__).resolve().parent / "model"
ROLLING_STATS = ["gf_rolling", "ga_rolling", "sh_rolling", "sot_rolling"]


def load():
    model = XGBClassifier()
    model.load_model(MODEL_DIR / "model.json")
    with open(MODEL_DIR / "teams.json") as f:
        data = json.load(f)
    return model, data


def predict_match(team1, team2, model, data):
    team1 = data["aliases"].get(team1, team1)
    team2 = data["aliases"].get(team2, team2)
    team1_data = data["teams"][team1]
    team2_data = data["teams"][team2]
    h2h_team1, h2h_team2 = data["h2h"][team1][team2]

    # One feature row, in the order used for training
    row = {
        "h/a": 1,  # Assume team1 is home
        "opp": team2_data["opp"],
        "hour": team1_data["hour"],
        "day": team1_data["day"],
        "team_elo": team1_data["team_elo"],
        "expected_score": team1_data["expected_score"],
        **{stat: team1_data[stat] for stat in ROLLING_STATS},
        "h2h_team1": h2h_team1,
        "h2h_team2": h2h_team2,
    }
    match_data = pd.DataFrame([row])[data["features"]]

    team1_prob = model.predict_proba(match_data)[0][1]  # Probability of team1 winning
    team2_prob = 1 - team1_prob  # Probability of team2 winning or draw

    if abs(team1_prob - team2_prob) < 0.1:  # If probabilities are close
        result = "Draw"
        prob = (team1_prob + team2_prob) / 2
    elif team1_prob > team2_prob:
        result = f"{team1} Wins"
        prob = team1_prob
    else:
        result = f"{team2} Wins"
        prob = team2_prob

    return {"Result": result, "Probability": float(prob)}


def main(argv):
    if len(argv) != 3:
        print("Usage: python predict.py <team1> <team2>", file=sys.stderr)
        return 2
    model, data = load()
    print(json.dumps(predict_match(argv[1], argv[2], model, data)))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
