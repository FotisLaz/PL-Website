#!/bin/bash

# Premier League Stats & Prediction Platform - Local Deployment Script
# This script sets up the complete microservices environment locally

set -e

echo "🚀 Starting Premier League Stats & Prediction Platform Deployment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
check_docker() {
    print_status "Checking Docker status..."
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    print_success "Docker is running"
}

# Check if Docker Compose is available
check_docker_compose() {
    print_status "Checking Docker Compose..."
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose and try again."
        exit 1
    fi
    print_success "Docker Compose is available"
}

# Clean up previous containers
cleanup() {
    print_status "Cleaning up previous containers..."
    docker-compose down --remove-orphans || true
    docker system prune -f || true
    print_success "Cleanup completed"
}

# Build and start services
start_services() {
    print_status "Building and starting microservices..."
    docker-compose up -d --build
    
    # Wait for services to be healthy
    print_status "Waiting for services to be healthy..."
    
    # Wait for Kafka
    print_status "Waiting for Kafka to start..."
    timeout 120s bash -c 'until docker-compose exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1; do sleep 2; done'
    print_success "Kafka is ready"
    
    # Wait for PostgreSQL
    print_status "Waiting for PostgreSQL to start..."
    timeout 60s bash -c 'until docker-compose exec postgres pg_isready -U postgres > /dev/null 2>&1; do sleep 2; done'
    print_success "PostgreSQL is ready"
    
    # Wait for Backend API
    print_status "Waiting for Backend API to start..."
    timeout 120s bash -c 'until curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; do sleep 5; done'
    print_success "Backend API is ready"
    
    # Wait for Data Processor
    print_status "Waiting for Data Processor to start..."
    timeout 120s bash -c 'until curl -f http://localhost:8081/actuator/health > /dev/null 2>&1; do sleep 5; done'
    print_success "Data Processor is ready"
    
    # Wait for Frontend
    print_status "Waiting for Frontend to start..."
    timeout 60s bash -c 'until curl -f http://localhost:3000 > /dev/null 2>&1; do sleep 2; done'
    print_success "Frontend is ready"
}

# Create Kafka topics
create_kafka_topics() {
    print_status "Creating Kafka topics..."
    
    # Create player-data topic
    docker-compose exec kafka kafka-topics --create \
        --topic player-data \
        --bootstrap-server localhost:9092 \
        --replication-factor 1 \
        --partitions 3 || true
    
    # Create match-data topic
    docker-compose exec kafka kafka-topics --create \
        --topic match-data \
        --bootstrap-server localhost:9092 \
        --replication-factor 1 \
        --partitions 3 || true
    
    print_success "Kafka topics created"
}

# Show service status
show_status() {
    print_status "Service Status:"
    echo "=================================="
    docker-compose ps
    echo "=================================="
    
    print_status "Available Services:"
    echo "🌐 Frontend:          http://localhost:3000"
    echo "🔧 Backend API:       http://localhost:8080"
    echo "⚙️  Data Processor:    http://localhost:8081"
    echo "📊 Kafka UI:          http://localhost:8090"
    echo "🗄️  PostgreSQL:       localhost:5432"
    echo "📨 Kafka:             localhost:9092"
    echo ""
    
    print_status "Health Checks:"
    echo "Backend API:      $(curl -s http://localhost:8080/actuator/health | jq -r '.status' 2>/dev/null || echo 'Not available')"
    echo "Data Processor:   $(curl -s http://localhost:8081/actuator/health | jq -r '.status' 2>/dev/null || echo 'Not available')"
    echo ""
    
    print_success "All services are running! 🎉"
    echo ""
    print_status "To run the web scraper:"
    echo "docker-compose --profile scraper up scraper"
    echo ""
    print_status "To view logs:"
    echo "docker-compose logs -f [service-name]"
    echo ""
    print_status "To stop all services:"
    echo "docker-compose down"
}

# Run scraper (optional)
run_scraper() {
    if [ "$1" = "--with-scraper" ]; then
        print_status "Running web scraper..."
        docker-compose --profile scraper up scraper
        print_success "Web scraper completed"
    fi
}

# Main execution
main() {
    echo "=================================================="
    echo "🏆 Premier League Stats & Prediction Platform"
    echo "   Microservices Deployment Script"
    echo "=================================================="
    echo ""
    
    check_docker
    check_docker_compose
    cleanup
    start_services
    create_kafka_topics
    show_status
    run_scraper "$1"
    
    print_success "Deployment completed successfully! 🚀"
    print_status "Press Ctrl+C to stop all services when done."
}

# Handle script arguments
case "$1" in
    --help|-h)
        echo "Usage: $0 [OPTIONS]"
        echo ""
        echo "Options:"
        echo "  --with-scraper    Also run the web scraper after deployment"
        echo "  --help, -h        Show this help message"
        echo ""
        echo "This script deploys the complete Premier League Stats platform"
        echo "with microservices architecture including Kafka, PostgreSQL,"
        echo "Backend API, Data Processor, and Frontend."
        exit 0
        ;;
    *)
        main "$1"
        ;;
esac 