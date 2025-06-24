-- Create player_stats table if it doesn't exist
CREATE TABLE IF NOT EXISTS player_stats (
    player_name VARCHAR(100) PRIMARY KEY,
    nation VARCHAR(50),
    position VARCHAR(10),
    age INTEGER,
    matches_played INTEGER,
    starts INTEGER,
    minutes_played FLOAT,
    goals FLOAT,
    assists FLOAT,
    penalties_scored FLOAT,
    yellow_cards FLOAT,
    red_cards FLOAT,
    expected_goals FLOAT,
    expected_assists FLOAT,
    team_name VARCHAR(100)
); 