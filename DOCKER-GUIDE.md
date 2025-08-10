# 🎲 Betting Game - Quick Start Guide

## For Testers & Reviewers

### Option 1: Docker (Recommended - 30 seconds)
```bash
# 1. Pull and run the betting game
docker run -p 8080:8080 victoria-2023/betting-game

# 2. Open browser
http://localhost:8080
```
That's it! No Java installation, no Maven, no configuration needed.

### Option 2: Traditional Setup (5-10 minutes)
```bash
# 1. Install Java 17
# 2. Install Maven 3.9+
# 3. Clone repository
git clone https://github.com/victoria-2023/betting-game.git
cd betting-game

# 4. Build and run
mvn clean package
java -jar target/betting-game-backend-1.0.0.jar

# 5. Open browser
http://localhost:8080
```

## For Developers

### Local Development with Docker
```bash
# Build your own image
docker build -f Dockerfile.simple -t betting-game-local .

# Run with live reload (mount source code)
docker run -p 8080:8080 -v $(pwd):/app betting-game-local

# Check logs
docker logs betting-game-container

# Stop when done
docker stop betting-game-container
```

## Features You Can Test
- 🎮 Real-time betting game
- 🔌 WebSocket connections
- 📊 Player statistics
- 🎯 Number guessing (1-10)
- 💰 Virtual currency system
- 📱 Responsive web interface

## API Endpoints
- `GET /` - Game interface
- `GET /actuator/health` - Health check
- `GET /h2-console` - Database console (dev)
- WebSocket: `/ws` - Real-time updates
