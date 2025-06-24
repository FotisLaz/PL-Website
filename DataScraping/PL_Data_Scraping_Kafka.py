##importing all required libraries
from bs4 import BeautifulSoup
import pandas as pd
import requests 
import time
import sys
import json
from kafka import KafkaProducer
from kafka.errors import KafkaError
import logging

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# Kafka configuration
KAFKA_BOOTSTRAP_SERVERS = ['localhost:9092']
KAFKA_TOPIC = 'player-data'

def create_kafka_producer():
    """Create and return a Kafka producer"""
    try:
        producer = KafkaProducer(
            bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
            value_serializer=lambda x: json.dumps(x).encode('utf-8'),
            key_serializer=lambda x: x.encode('utf-8') if x else None,
            retries=3,
            acks='all'
        )
        logger.info("Kafka producer created successfully")
        return producer
    except Exception as e:
        logger.error(f"Failed to create Kafka producer: {e}")
        raise

def send_to_kafka(producer, player_data, team_name):
    """Send player data to Kafka topic"""
    try:
        # Create message key using player name and team
        key = f"{player_data.get('name', 'unknown')}_{team_name}"
        
        # Send message to Kafka
        future = producer.send(KAFKA_TOPIC, key=key, value=player_data)
        
        # Block for 'synchronous' sends
        record_metadata = future.get(timeout=10)
        logger.info(f"Message sent to topic {record_metadata.topic} partition {record_metadata.partition} offset {record_metadata.offset}")
        
    except KafkaError as e:
        logger.error(f"Failed to send message to Kafka: {e}")
        raise
    except Exception as e:
        logger.error(f"Unexpected error sending to Kafka: {e}")
        raise

def process_player_row(row, team_name):
    """Process a single player row and return structured data"""
    try:
        # Handle potential parsing issues
        def safe_parse_int(value):
            try:
                return int(value) if value and str(value).strip() != '' else None
            except (ValueError, TypeError):
                return None
        
        def safe_parse_float(value):
            try:
                return float(value) if value and str(value).strip() != '' else None
            except (ValueError, TypeError):
                return None
        
        player_data = {
            "name": str(row.get('Player', '')).strip(),
            "nation": str(row.get('Nation', '')).strip(),
            "pos": str(row.get('Pos', '')).strip(),
            "age": safe_parse_int(row.get('Age')),
            "mp": safe_parse_int(row.get('MP')),
            "starts": safe_parse_int(row.get('Starts')),
            "min": safe_parse_float(row.get('Min')),
            "gls": safe_parse_float(row.get('Gls')),
            "ast": safe_parse_float(row.get('Ast')),
            "pk": safe_parse_float(row.get('PK')),
            "crdy": safe_parse_float(row.get('CrdY')),
            "crdr": safe_parse_float(row.get('CrdR')),
            "xg": safe_parse_float(row.get('xG')),
            "xa": safe_parse_float(row.get('xAG')),
            "team": team_name
        }
        
        # Skip if player name is empty or contains summary data
        if not player_data["name"] or "Squad Total" in player_data["name"]:
            return None
            
        return player_data
        
    except Exception as e:
        logger.error(f"Error processing player row: {e}")
        return None

# Initialize Kafka producer
producer = create_kafka_producer()

all_teams = []
headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'}

# --- Get Main League Page --- 
try:
    logger.info("Fetching main league page...")
    response = requests.get('https://fbref.com/en/comps/9/Premier-League-Stats', headers=headers)
    response.raise_for_status()
    html = response.text
    logger.info("Main page fetched successfully.")
except requests.exceptions.RequestException as e:
    logger.error(f"Error fetching main league page: {e}")
    sys.exit(1)

soup = BeautifulSoup(html, 'lxml')

# --- Find Team Links --- 
try:
    table = soup.find_all('table', class_='stats_table')[0]
    links = table.find_all('a')
    links = [l.get("href") for l in links]
    links = [l for l in links if l and '/squads/' in l]
    if not links:
        logger.error("Could not find any team squad links on the main page.")
        sys.exit(1)
    team_urls = [f"https://fbref.com{l}" for l in links]
    logger.info(f"Found {len(team_urls)} team URLs.")
except IndexError:
    logger.error("Could not find the expected table ('stats_table') on the main page.")
    sys.exit(1)
except Exception as e:
    logger.error(f"An unexpected error occurred while extracting team links: {e}")
    sys.exit(1)

# --- Loop Through Teams --- 
successful_teams = 0
total_players_sent = 0

for team_url in team_urls:
    try:
        team_name = team_url.split("/")[-1].replace("-Stats", "").replace("-", " ")
        logger.info(f"Fetching stats for {team_name}...")
        
        response = requests.get(team_url, headers=headers)
        response.raise_for_status()
        data = response.text
        soup_team = BeautifulSoup(data, 'lxml')
        
        # Find the main player stats table
        stats_table_tag = soup_team.find_all('table', class_="stats_table")[0] 

        # Read HTML table into DataFrame
        team_data = pd.read_html(str(stats_table_tag))[0]
        
        # Handle MultiIndex columns
        if isinstance(team_data.columns, pd.MultiIndex):
            logger.info(f"Dropping header level for {team_name}.")
            team_data.columns = team_data.columns.droplevel()

        # Process each player and send to Kafka
        players_sent_for_team = 0
        for index, row in team_data.iterrows():
            player_data = process_player_row(row, team_name)
            if player_data:
                try:
                    send_to_kafka(producer, player_data, team_name)
                    players_sent_for_team += 1
                    total_players_sent += 1
                except Exception as e:
                    logger.error(f"Failed to send player {player_data.get('name')} to Kafka: {e}")
        
        logger.info(f"Successfully processed {team_name} - sent {players_sent_for_team} players to Kafka.")
        successful_teams += 1
        
        # Add a delay to be polite to the server
        time.sleep(3)

    except requests.exceptions.RequestException as e:
        logger.error(f"Error fetching team {team_name}: {e}. Skipping this team.")
        continue
    except IndexError:
        logger.error(f"Could not find 'stats_table' for team {team_name}. Skipping this team.")
        continue
    except Exception as e:
        logger.error(f"An unexpected error occurred processing team {team_name}: {e}. Skipping this team.")
        continue

# Close the producer
try:
    producer.flush()
    producer.close()
    logger.info("Kafka producer closed successfully.")
except Exception as e:
    logger.error(f"Error closing Kafka producer: {e}")

# Summary
logger.info(f"Scraping completed!")
logger.info(f"Successfully processed {successful_teams} teams")
logger.info(f"Total players sent to Kafka: {total_players_sent}")

if successful_teams == 0:
    logger.error("No team data was successfully scraped and sent to Kafka.")
    sys.exit(1)
else:
    logger.info("Data successfully sent to Kafka for processing by the data-processor-service.") 