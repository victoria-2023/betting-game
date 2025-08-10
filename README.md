# Betting Game Backend

A Spring Boot backend application for a real-time betting game with WebSocket communication.

## Game Process

1. The server starts a round of the game and gives 10 seconds to place a bet for the players on numbers from 1 to 10 with the amount of the bet
2. After the time expires, the server generates a random number from 1 to 10
3. If the player guesses the number, a message is sent to him that he won with winnings of 9.9 times the stake
4. If the player loses receives a message about the loss
5. All players receive a message with a list of winning players in which there is a nickname and the amount of winnings
6. The process is repeated

## Technology Stack

- **Java 17**
- **Spring Boot 3.1.5**
- **Spring WebSocket** for real-time communication
- **Spring Data JPA** for data persistence
- **H2 Database** (in-memory for development)
- **Maven** for dependency management
- **JUnit 5** and **Mockito** for testing
- **Lombok** for reducing boilerplate code

## Features

- ✅ Player registration with unique nicknames
- ✅ Real-time betting with 10-second rounds
- ✅ WebSocket communication for live updates
- ✅ Random number generation (1-10)
- ✅ 9.9x multiplier for winning bets
- ✅ Player balance management
- ✅ Comprehensive input validation
- ✅ Unit and integration tests
- ✅ Performance test for RTP calculation
- ✅ No STOMP usage (pure WebSocket)

## API Endpoints

### Player Management
- `POST /api/players/register` - Register a new player
- `GET /api/players/{nickname}` - Get player information
- `GET /api/players/winners` - Get top winners

### Game Operations
- `POST /api/bets` - Place a bet
- `GET /api/game/state` - Get current game state

### WebSocket Topics
- `/topic/game-state` - Game state updates (countdown, betting status)
- `/topic/round-results` - Round results with winners
- `/topic/bet-placed` - Bet placement notifications

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Running the Application

1. **Clone and navigate to the project:**
   ```bash
   git clone <repository-url>
   cd betting-game
   ```

2. **Build the project:**
   ```bash
   mvn clean compile
   ```

3. **Run tests:**
   ```bash
   mvn test
   ```

4. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

### Database Console
Access the H2 database console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:bettingdb`
- Username: `sa`
- Password: (empty)

## Testing

### Unit Tests
```bash
mvn test -Dtest="*Test"
```

### Integration Tests
```bash
mvn test -Dtest="*IntegrationTest"
```

### RTP Performance Test (Optional)
Run the million-round RTP calculation test:
```bash
mvn test -Dtest="RTPCalculationTest"
```

This test simulates 1 million betting rounds across 24 threads to calculate the Return to Player (RTP) percentage, which should be approximately 99%.

## WebSocket Connection

### JavaScript Example
```javascript
// Connect to WebSocket
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to game state updates
    stompClient.subscribe('/topic/game-state', function (message) {
        const gameState = JSON.parse(message.body);
        console.log('Game State:', gameState);
    });
    
    // Subscribe to round results
    stompClient.subscribe('/topic/round-results', function (message) {
        const results = JSON.parse(message.body);
        console.log('Round Results:', results);
    });
});
```

## API Usage Examples

### Register a Player
```bash
curl -X POST http://localhost:8080/api/players/register \
  -H "Content-Type: application/json" \
  -d '{"nickname": "player1", "initialBalance": 1000}'
```

### Place a Bet
```bash
curl -X POST http://localhost:8080/api/bets \
  -H "Content-Type: application/json" \
  -d '{"nickname": "player1", "betNumber": 7, "betAmount": 100}'
```

### Get Game State
```bash
curl http://localhost:8080/api/game/state
```

## Data Models

### Player
- `id`: Unique identifier
- `nickname`: Unique player name (3-20 characters)
- `balance`: Current balance
- `totalWinnings`: Total amount won
- `createdAt`: Registration timestamp

### Bet
- `id`: Unique identifier
- `player`: Reference to player
- `gameRound`: Reference to game round
- `betNumber`: Number bet on (1-10)
- `betAmount`: Amount wagered
- `winnings`: Amount won (if winner)
- `isWinner`: Win/loss status

### GameRound
- `id`: Unique identifier
- `winningNumber`: Generated winning number
- `startTime`: Round start time
- `endTime`: Round end time
- `status`: BETTING_OPEN, BETTING_CLOSED, COMPLETED

## Game Rules

1. **Betting Window**: Players have exactly 10 seconds to place bets
2. **Bet Numbers**: Valid numbers are 1-10
3. **Win Multiplier**: 9.9x the bet amount
4. **One Bet Per Round**: Each player can only bet once per round
5. **Balance Check**: Players must have sufficient balance
6. **Win Probability**: 1/10 (10%) chance to win
7. **Expected RTP**: 99% (9.9x multiplier × 10% chance)

## Architecture

The application follows a layered architecture:

- **Controller Layer**: REST API endpoints and WebSocket handlers
- **Service Layer**: Business logic and game management
- **Repository Layer**: Data access using Spring Data JPA
- **Model Layer**: Entity classes representing game data
- **DTO Layer**: Data transfer objects for API communication

## Configuration

Key configuration properties in `application.properties`:

- `server.port=8080`: Application port
- `spring.jpa.hibernate.ddl-auto=create-drop`: Database schema management
- `spring.h2.console.enabled=true`: Enable H2 console for development
- `logging.level.com.bettinggame=DEBUG`: Application logging level

## Error Handling

The application includes comprehensive error handling:

- Input validation with custom error messages
- Global exception handler for consistent error responses
- Proper HTTP status codes
- Detailed logging for debugging

## Performance Considerations

- Connection pooling for database access
- Efficient WebSocket message broadcasting
- Optimized JPA queries with proper indexing
- Thread-safe game round management
- Background scheduling for automatic round progression

## Deployment

The application is ready for deployment on various platforms. Deployment files are included for:

### Quick Deploy Options

#### 1. Railway (Recommended for beginners)
1. Push code to GitHub
2. Connect Railway to your GitHub repo
3. Deploy automatically using `railway.toml`

#### 2. Render
1. Push code to GitHub
2. Connect Render to your GitHub repo
3. Deploy using `render.yaml`

#### 3. Heroku
1. Install Heroku CLI
2. Create Heroku app: `heroku create your-betting-game`
3. Push code: `git push heroku main`

#### 4. Docker
```bash
# Build and run locally
docker build -t betting-game .
docker run -p 8080:8080 betting-game

# Or use Docker Compose
docker-compose up --build
```

### Environment Variables for Production

- `PORT`: Application port (default: 8080)
- `DATABASE_URL`: Database connection string
- `DATABASE_USERNAME`: Database username
- `DATABASE_PASSWORD`: Database password
- `SPRING_PROFILES_ACTIVE`: Set to `prod` for production

### Production URLs
After deployment, access:
- **Game Interface**: `https://your-app-url.com`
- **Health Check**: `https://your-app-url.com/actuator/health`
- **WebSocket**: `wss://your-app-url.com/ws`

## Future Enhancements

Potential improvements for production deployment:

1. **Security**: Add authentication and authorization
2. **Database**: Configure PostgreSQL or MySQL for production
3. **Monitoring**: Add metrics and health checks
4. **Caching**: Implement Redis for session management
5. **Load Balancing**: Support multiple application instances
6. **Rate Limiting**: Prevent excessive betting requests
7. **Audit Trail**: Track all game activities
8. **Admin Panel**: Web interface for game management
