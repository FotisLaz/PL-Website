import pandas as pd
from xgboost import XGBClassifier
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split
from sklearn.metrics import precision_score
import sys
import json
import numpy as np

# Load data
matches = pd.read_csv("matches.csv", index_col=0)

# Preprocess data
matches["date"] = pd.to_datetime(matches["date"])
matches["h/a"] = matches["venue"].astype("category").cat.codes
matches["opp"] = matches["opponent"].astype("category").cat.codes
matches["hour"] = matches["time"].str.replace(":.+", "", regex=True).astype("int")
matches["day"] = matches["date"].dt.dayofweek
matches["target"] = (matches["result"] == "W").astype("int")  # 1: Win, 0: Lose/Draw

# Map team names for consistency
map_values = {
    "Brighton and Hove Albion": "Brighton",
    "Manchester United": "Manchester Utd",
    "Tottenham Hotspur": "Tottenham",
    "West Ham United": "West Ham",
    "Wolverhampton Wanderers": "Wolves",
    "Newcastle United": "Newcastle Utd"  # Χάρτης ονομάτων
}

matches["team"] = matches["team"].map(map_values).fillna(matches["team"])
matches["opponent"] = matches["opponent"].map(map_values).fillna(matches["opponent"])

# Add additional features
# Elo Ratings

def calculate_elo(team, opponent, current_elo):
    k = 20
    # Προσθήκη ομάδων που λείπουν
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
# Προσθήκη αντιπάλων που δεν περιλαμβάνονται ήδη
for opponent in matches["opponent"].unique():
    if opponent not in elo_ratings:
        elo_ratings[opponent] = 1500

matches = matches.sort_values("date")
elo_history = []

for index, row in matches.iterrows():
    team = row["team"]
    opponent = row["opponent"]

    # Προσθήκη ομάδων στο λεξικό αν δεν υπάρχουν
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

# Evaluate model
preds = model.predict(test_X)
probs = model.predict_proba(test_X)
accuracy = accuracy_score(test_y, preds)
precision = precision_score(test_y, preds)
print(f"Model Accuracy: {accuracy:.2f}")
print(f"Model Precision: {precision:.2f}")
# Prediction function
def predict_match(team1, team2):
    team1 = map_values.get(team1, team1)
    team2 = map_values.get(team2, team2)

    # Get the most recent data for both teams
    team1_data = matches[matches["team"] == team1].sort_values("date").iloc[-1]
    team2_data = matches[matches["team"] == team2].sort_values("date").iloc[-1]

    # Create features for prediction in the exact order used during training
    match_data = pd.DataFrame([{
        "h/a": 1,  # Assume team1 is home
        "opp": team2_data["opp"],
        "hour": team1_data["hour"],
        "day": team1_data["day"],
        "team_elo": team1_data["team_elo"],
        "expected_score": team1_data["expected_score"],
        **{stat: team1_data[stat] for stat in new_stats},
        "h2h_team1": calculate_head_to_head(matches, team1, team2)[0],
        "h2h_team2": calculate_head_to_head(matches, team1, team2)[1]
    }])[features]  # Ensure the order matches the training features

    # Predict probabilities
    match_probs = model.predict_proba(match_data)[0]
    team1_prob = match_probs[1]  # Probability of team1 winning
    team2_prob = 1 - team1_prob  # Probability of team2 winning or draw

    # Determine result
    if abs(team1_prob - team2_prob) < 0.1:  # If probabilities are close
        result = "Draw"
        prob = (team1_prob + team2_prob) / 2
    elif team1_prob > team2_prob:
        result = f"{team1} Wins"
        prob = team1_prob
    else:
        result = f"{team2} Wins"
        prob = team2_prob

    return {
        "Result": result,
        "Probability": prob
    }

# Helper function to convert numpy types to native Python types for JSON serialization
def convert_numpy_types(obj):
    if isinstance(obj, dict):
        return {k: convert_numpy_types(v) for k, v in obj.items()}
    elif isinstance(obj, list):
        return [convert_numpy_types(i) for i in obj]
    elif isinstance(obj, (np.int_, np.intc, np.intp, np.int8, np.int16, np.int32, np.int64, np.uint8,
                        np.uint16, np.uint32, np.uint64)):
        return int(obj)
    elif isinstance(obj, (np.float16, np.float32, np.float64)):
        return float(obj)
    elif isinstance(obj, (np.ndarray,)):
        return obj.tolist()
    elif isinstance(obj, (np.bool_)):
        return bool(obj)
    elif isinstance(obj, (np.void)):
        return None
    return obj

# Example usage
if __name__ == "__main__":
    import sys
    if len(sys.argv) != 3:
        print(json.dumps({"Error": "Usage: python pred.py <Team1> <Team2>"}))
    else:
        team1 = sys.argv[1]
        team2 = sys.argv[2]
        prediction_raw = predict_match(team1, team2)
        # Convert numpy types before dumping to JSON
        prediction_serializable = convert_numpy_types(prediction_raw)
        print(json.dumps(prediction_serializable))