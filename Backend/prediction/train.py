"""Trains the match prediction model and saves it for predict.py.

Runs once when the backend image is built (see Backend/Dockerfile), not on every request.
Writes model/model.json (the XGBoost model, in XGBoost's own format) and model/teams.json
(the per-team values predict.py needs to build the feature row of a match).
"""
import json
from pathlib import Path

import pandas as pd
from sklearn.metrics import accuracy_score, precision_score
from xgboost import XGBClassifier

HERE = Path(__file__).resolve().parent
MODEL_DIR = HERE / "model"

# Load data
matches = pd.read_csv(HERE / "matches.csv", index_col=0)

# Preprocess data
matches["date"] = pd.to_datetime(matches["date"])
matches["h/a"] = matches["venue"].astype("category").cat.codes
matches["opp"] = matches["opponent"].astype("category").cat.codes
matches["hour"] = matches["time"].str.replace(":.+", "", regex=True).astype("int")
matches["day"] = matches["date"].dt.dayofweek
matches["target"] = (matches["result"] == "W").astype("int")  # 1: Win, 0: Lose/Draw

# Map team names for consistency; the long names stay accepted as aliases in predict.py
map_values = {
    "Brighton and Hove Albion": "Brighton",
    "Manchester United": "Manchester Utd",
    "Tottenham Hotspur": "Tottenham",
    "West Ham United": "West Ham",
    "Wolverhampton Wanderers": "Wolves",
    "Newcastle United": "Newcastle Utd",
    # The opponent column already uses the short names of these two as well
    "Sheffield United": "Sheffield Utd",
    "West Bromwich Albion": "West Brom"
}

matches["team"] = matches["team"].map(map_values).fillna(matches["team"])
matches["opponent"] = matches["opponent"].map(map_values).fillna(matches["opponent"])


# Elo ratings
def calculate_elo(team, opponent, current_elo):
    # Add teams that are missing
    if team not in current_elo:
        current_elo[team] = 1500
    if opponent not in current_elo:
        current_elo[opponent] = 1500
    expected_score = 1 / (1 + 10 ** ((current_elo[opponent] - current_elo[team]) / 400))
    return current_elo[team], expected_score


def update_elo(team, opponent, result, current_elo):
    k = 20
    expected_score = 1 / (1 + 10 ** ((current_elo[opponent] - current_elo[team]) / 400))
    actual_score = result  # 1 for win, 0.5 for draw, 0 for loss
    current_elo[team] += k * (actual_score - expected_score)
    current_elo[opponent] += k * ((1 - actual_score) - (1 - expected_score))
    return current_elo


def calculate_head_to_head(matches, team1, team2):
    h2h_matches = matches[((matches["team"] == team1) & (matches["opponent"] == team2)) |
                          ((matches["team"] == team2) & (matches["opponent"] == team1))]
    h2h_wins_team1 = h2h_matches[h2h_matches["team"] == team1]["target"].sum()
    h2h_wins_team2 = h2h_matches[h2h_matches["team"] == team2]["target"].sum()
    h2h_total = len(h2h_matches)
    if h2h_total > 0:
        return h2h_wins_team1 / h2h_total, h2h_wins_team2 / h2h_total
    return 0.5, 0.5


teams = matches["team"].unique()
elo_ratings = {team: 1500 for team in teams}
for opponent in matches["opponent"].unique():
    if opponent not in elo_ratings:
        elo_ratings[opponent] = 1500

matches = matches.sort_values("date")
elo_history = []

for index, row in matches.iterrows():
    team = row["team"]
    opponent = row["opponent"]

    if team not in elo_ratings:
        elo_ratings[team] = 1500
    if opponent not in elo_ratings:
        elo_ratings[opponent] = 1500

    result = row["target"]
    if result == 1:
        actual_score = 1
    elif row["result"] == "D":
        actual_score = 0.5
    else:
        actual_score = 0

    team_elo, expected_score = calculate_elo(team, opponent, elo_ratings)
    elo_history.append((team_elo, expected_score))
    elo_ratings = update_elo(team, opponent, actual_score, elo_ratings)

matches["team_elo"] = [h[0] for h in elo_history]
matches["expected_score"] = [h[1] for h in elo_history]

# Rolling averages for team stats
stats = ["gf", "ga", "sh", "sot"]  # Goals for, goals against, shots, shots on target
new_stats = [f"{stat}_rolling" for stat in stats]


def calculate_rolling_stats(matches, stats, new_stats):
    for stat, new_stat in zip(stats, new_stats):
        matches[new_stat] = matches.groupby("team")[stat].transform(lambda x: x.rolling(5, min_periods=1).mean())
    return matches


matches = calculate_rolling_stats(matches, stats, new_stats)

# Add Head-to-Head stats
matches["h2h_team1"] = matches.apply(lambda row: calculate_head_to_head(matches, row["team"], row["opponent"])[0], axis=1)
matches["h2h_team2"] = matches.apply(lambda row: calculate_head_to_head(matches, row["team"], row["opponent"])[1], axis=1)

# Select features
features = ["h/a", "opp", "hour", "day", "team_elo", "expected_score", "h2h_team1", "h2h_team2"] + new_stats

# Train-test split
train = matches[matches["date"].dt.year == 2021]
test = matches[matches["date"].dt.year == 2022]

train_X = train[features]
train_y = train["target"]
test_X = test[features]
test_y = test["target"]

# Train model
model = XGBClassifier(n_estimators=50, max_depth=3, learning_rate=0.1, random_state=42)
model.fit(train_X, train_y)

# Evaluate model on the test set
preds = model.predict(test_X)
accuracy = accuracy_score(test_y, preds)
precision = precision_score(test_y, preds)
print(f"Test set ({len(test)} rows): accuracy {accuracy:.4f}, precision {precision:.4f}")

# The "opp" feature value of each team, i.e. its code when it is the opponent
opp_codes = dict(zip(matches["opponent"], matches["opp"]))

# What predict.py needs per team: its "opp" code (used when it is team2) and the values of its
# most recent match (used when it is team1)
team_data = {}
for team in sorted(matches["team"].unique()):
    latest = matches[matches["team"] == team].sort_values("date").iloc[-1]
    team_data[team] = {
        "opp": int(opp_codes[team]),
        "hour": int(latest["hour"]),
        "day": int(latest["day"]),
        "team_elo": float(latest["team_elo"]),
        "expected_score": float(latest["expected_score"]),
        **{stat: float(latest[stat]) for stat in new_stats},
    }

# Head-to-head share of wins for every ordered pair of teams
h2h = {}
for team1 in team_data:
    h2h[team1] = {}
    for team2 in team_data:
        if team1 != team2:
            wins1, wins2 = calculate_head_to_head(matches, team1, team2)
            h2h[team1][team2] = [float(wins1), float(wins2)]

MODEL_DIR.mkdir(exist_ok=True)
model.save_model(MODEL_DIR / "model.json")
with open(MODEL_DIR / "teams.json", "w") as f:
    json.dump({"features": features, "aliases": map_values, "teams": team_data, "h2h": h2h}, f, indent=1)
print(f"Saved the model and {len(team_data)} teams to {MODEL_DIR}")
