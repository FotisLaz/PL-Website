"""Tests predict.py through its command line, the way PredictionService calls it.

Needs the trained model: run `python train.py` in Backend/prediction first (CI does).
"""
import json
import subprocess
import sys
from pathlib import Path

import pytest

PREDICTION_DIR = Path(__file__).resolve().parent.parent


@pytest.fixture(scope="module", autouse=True)
def trained_model():
    if not (PREDICTION_DIR / "model" / "model.json").exists():
        pytest.fail("No trained model: run 'python train.py' in Backend/prediction first")


def run_predict(team1, team2):
    """Returns (exit code, parsed JSON from stdout, stderr)."""
    result = subprocess.run([sys.executable, str(PREDICTION_DIR / "predict.py"), team1, team2],
                            capture_output=True, text=True, timeout=60)
    return result.returncode, json.loads(result.stdout), result.stderr


def test_known_pair_returns_a_prediction():
    code, output, _ = run_predict("Arsenal", "Chelsea")

    assert code == 0
    assert output["Result"] in ("Arsenal Wins", "Chelsea Wins", "Draw")
    assert 0 <= output["Probability"] <= 1


def test_unknown_team_fails_cleanly():
    code, output, stderr = run_predict("Arsnal", "Chelsea")

    assert code == 3
    assert output == {"error": "Unknown team: Arsnal"}
    assert "Traceback" not in stderr


def test_team_names_ignore_letter_case_and_spaces():
    assert run_predict(" arsenal ", "CHELSEA") == run_predict("Arsenal", "Chelsea")


def test_long_team_names_are_accepted():
    assert run_predict("Manchester United", "Chelsea") == run_predict("Manchester Utd", "Chelsea")


def test_same_team_twice_is_rejected():
    code, output, _ = run_predict("Arsenal", "arsenal")

    assert code == 3
    assert "different teams" in output["error"]
