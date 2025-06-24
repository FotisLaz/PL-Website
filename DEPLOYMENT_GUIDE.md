# 🚀 Premier League Stats Platform - Deployment Guide

This guide provides step-by-step instructions for deploying the Premier League Stats & Prediction Platform with its modern microservices architecture.

## 🏗️ Architecture Overview

The platform consists of the following microservices:

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Web Scraper   │───▶│     Kafka        │───▶│ Data Processor  │
│   (Python)      │    │   Message Bus    │    │  Microservice   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │                        │
                                │                        ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Frontend      │───▶│   Backend API    │───▶│   PostgreSQL    │
│   (React)       │    │ (Spring Boot)    │    │    Database     │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## 📋 Prerequisites

### Required Software

- **Docker** (v20.10+)
- **Docker Compose** (v2.0+)
- **Git** (for cloning the repository)

### Optional (for manual development)

- **Java 17+**
- **Node.js 18+**
- **Python 3.8+**
- **PostgreSQL 12+**
- **Maven 3.6+**

## 🚀 Quick Start (Recommended)

### 1. Clone the Repository

```bash
git clone <repository-url>
cd premiere
```

### 2. Make Deployment Script Executable

```bash
chmod +x deploy-local.sh
```

### 3. Deploy the Platform

```bash
./deploy-local.sh
```

This will:

- ✅ Check Docker prerequisites
- ✅ Clean up any existing containers
- ✅ Build and start all microservices
- ✅ Create Kafka topics
- ✅ Perform health checks
- ✅ Display service URLs and status

### 4. Access the Application

Once deployment is complete, access:

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Data Processor**: http://localhost:8081
- **Kafka UI**: http://localhost:8090

### 5. Run Web Scraper (Optional)

```bash
docker-compose --profile scraper up scraper
```

## 🔧 Manual Deployment

If you prefer manual control over the deployment process:

### 1. Start Infrastructure Services

```bash
# Start PostgreSQL, Kafka, and Zookeeper
docker-compose up -d postgres kafka zookeeper
```

### 2. Wait for Infrastructure

```bash
# Wait for PostgreSQL
docker-compose exec postgres pg_isready -U postgres

# Wait for Kafka
docker-compose exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### 3. Start Application Services

```bash
# Start backend services
docker-compose up -d backend-api data-processor

# Start frontend
docker-compose up -d frontend
```

### 4. Start Monitoring

```bash
# Start Kafka UI for monitoring
docker-compose up -d kafka-ui
```

## 📊 Service Details

### Backend API (Port 8080)

- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **API Documentation**: Available via Swagger (if configured)

### Data Processor (Port 8081)

- **Health Check**: http://localhost:8081/actuator/health
- **Kafka Consumer**: Processes `player-data` and `match-data` topics

### Frontend (Port 3000)

- **Main Application**: http://localhost:3000
- **Built with**: React, SASS, React Router
- **Nginx Reverse Proxy**: Routes API calls to backend

### Kafka UI (Port 8090)

- **Web Interface**: http://localhost:8090
- **Monitor Topics**: View messages, consumer groups, and broker status

## 🔍 Monitoring and Troubleshooting

### View Service Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend-api
docker-compose logs -f data-processor
docker-compose logs -f frontend
```

### Check Service Status

```bash
docker-compose ps
```

### Health Checks

```bash
# Backend API
curl http://localhost:8080/actuator/health

# Data Processor
curl http://localhost:8081/actuator/health

# Frontend
curl http://localhost:3000
```

### Kafka Topics

```bash
# List topics
docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092

# View messages in player-data topic
docker-compose exec kafka kafka-console-consumer \
  --topic player-data \
  --bootstrap-server localhost:9092 \
  --from-beginning
```

## 🛠️ Development Mode

For development with hot reloading:

### Frontend Development

```bash
cd Frontend
npm install
npm start
```

### Backend Development

```bash
cd Backend
mvn spring-boot:run
```

### Data Processor Development

```bash
cd Backend/data-processor-service
mvn spring-boot:run
```

## 🌐 Production Deployment

### AWS Deployment

The platform includes CI/CD pipeline for AWS deployment:

1. **Setup AWS Infrastructure**:

   - EC2 instances
   - RDS PostgreSQL
   - ECR repositories
   - Security Groups

2. **Configure GitHub Secrets**:

   ```
   AWS_ACCESS_KEY_ID
   AWS_SECRET_ACCESS_KEY
   EC2_SSH_KEY
   EC2_HOST
   EC2_USER
   DATABASE_URL
   DATABASE_USERNAME
   DATABASE_PASSWORD
   ```

3. **Deploy via GitHub Actions**:
   - Push to `main` branch triggers deployment
   - Builds Docker images
   - Pushes to ECR
   - Deploys to EC2 with zero downtime

### Production Configuration

```bash
# Use production compose file
docker-compose -f docker-compose.prod.yml up -d
```

## 🧪 Testing

### Run All Tests

```bash
# Backend tests
cd Backend && mvn test
cd Backend/data-processor-service && mvn test

# Frontend tests
cd Frontend && npm test
```

### Integration Testing

```bash
# Test complete flow with Kafka
./deploy-local.sh --with-scraper
```

## 🔄 Data Flow Testing

1. **Start the platform**:

   ```bash
   ./deploy-local.sh
   ```

2. **Run web scraper**:

   ```bash
   docker-compose --profile scraper up scraper
   ```

3. **Monitor Kafka messages**:

   - Open Kafka UI: http://localhost:8090
   - Check `player-data` topic for messages

4. **Verify data processing**:
   - Check data processor logs
   - Verify data in PostgreSQL
   - Test API endpoints

## 🛑 Stopping Services

### Stop All Services

```bash
docker-compose down
```

### Stop and Remove Volumes

```bash
docker-compose down -v
```

### Complete Cleanup

```bash
docker-compose down -v --remove-orphans
docker system prune -f
```

## 🔧 Configuration

### Environment Variables

Create `.env` file for custom configuration:

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/prem_stats
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=password

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Spring Profiles
SPRING_PROFILES_ACTIVE=dev
```

### Custom Docker Compose

Override default configuration:

```bash
# Create docker-compose.override.yml
version: '3.8'
services:
  backend-api:
    environment:
      - SPRING_PROFILES_ACTIVE=custom
```

## 📚 Additional Resources

- **Architecture Documentation**: See README.md
- **API Documentation**: Available at backend endpoints
- **Kafka Documentation**: https://kafka.apache.org/documentation/
- **Spring Boot Documentation**: https://spring.io/projects/spring-boot
- **React Documentation**: https://reactjs.org/docs/

## 🆘 Common Issues

### Issue: Kafka Connection Failed

**Solution**: Ensure Kafka is fully started before other services

```bash
docker-compose up -d kafka
# Wait for Kafka to be ready
docker-compose up -d backend-api data-processor
```

### Issue: Database Connection Error

**Solution**: Check PostgreSQL is running and accessible

```bash
docker-compose exec postgres pg_isready -U postgres
```

### Issue: Port Already in Use

**Solution**: Stop conflicting services or change ports in docker-compose.yml

### Issue: Out of Memory

**Solution**: Increase Docker memory allocation or reduce service resources

---

## 🎯 Next Steps

After successful deployment:

1. **Explore the Frontend**: Browse player statistics and try match predictions
2. **Monitor with Kafka UI**: Watch real-time message processing
3. **Check Health Endpoints**: Verify all services are healthy
4. **Run Data Collection**: Execute the web scraper to populate data
5. **Test API Endpoints**: Use the backend API for custom queries

**Happy Deploying! 🚀**
