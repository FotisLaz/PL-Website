FROM python:3.11-slim

# Set working directory
WORKDIR /app

# Install system dependencies
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first for better caching
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the scraper script
COPY PL_Data_Scraping_Kafka.py .

# Create non-root user
RUN adduser --disabled-password --gecos '' scraper
USER scraper

# Run the scraper
CMD ["python", "PL_Data_Scraping_Kafka.py"] 