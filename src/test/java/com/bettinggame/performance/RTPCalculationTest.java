package com.bettinggame.performance;

import com.bettinggame.dto.BetRequest;
import com.bettinggame.dto.PlayerRegistrationRequest;
import com.bettinggame.model.GameRound;
import com.bettinggame.model.Player;
import com.bettinggame.service.GameService;
import com.bettinggame.service.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class RTPCalculationTest {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private GameService gameService;

    private static final int TOTAL_ROUNDS = 1_000_000;
    private static final int THREAD_COUNT = 24;
    private static final BigDecimal BET_AMOUNT = BigDecimal.valueOf(100);

    @Test
    @Transactional
    void calculateRTP_OneMillion_Rounds() throws InterruptedException, ExecutionException {
        log.info("Starting RTP calculation test with {} rounds using {} threads", TOTAL_ROUNDS, THREAD_COUNT);

        // Create test players
        List<Player> players = createTestPlayers();
        
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<RoundResult>> futures = new ArrayList<>();

        int roundsPerThread = TOTAL_ROUNDS / THREAD_COUNT;
        
        long startTime = System.currentTimeMillis();

        // Submit tasks to threads
        for (int thread = 0; thread < THREAD_COUNT; thread++) {
            final int threadId = thread;
            Future<RoundResult> future = executor.submit(() -> simulateRounds(players, roundsPerThread, threadId));
            futures.add(future);
        }

        // Collect results
        BigDecimal totalBetsAmount = BigDecimal.ZERO;
        BigDecimal totalWinningsAmount = BigDecimal.ZERO;
        int totalBets = 0;
        int totalWins = 0;

        for (Future<RoundResult> future : futures) {
            RoundResult result = future.get();
            totalBetsAmount = totalBetsAmount.add(result.totalBets);
            totalWinningsAmount = totalWinningsAmount.add(result.totalWinnings);
            totalBets += result.betCount;
            totalWins += result.winCount;
        }

        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Calculate RTP
        BigDecimal rtp = totalWinningsAmount.divide(totalBetsAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        log.info("=== RTP CALCULATION RESULTS ===");
        log.info("Total rounds simulated: {}", TOTAL_ROUNDS);
        log.info("Total bets placed: {}", totalBets);
        log.info("Total wins: {}", totalWins);
        log.info("Win percentage: {:.2f}%", (double) totalWins / totalBets * 100);
        log.info("Total amount bet: ${}", totalBetsAmount);
        log.info("Total amount won: ${}", totalWinningsAmount);
        log.info("RTP (Return to Player): {:.2f}%", rtp);
        log.info("Expected RTP: 99.00% (9.9x multiplier with 1/10 win chance)");
        log.info("Test duration: {} ms", duration);
        log.info("Rounds per second: {}", TOTAL_ROUNDS * 1000L / duration);

        // Verify RTP is close to expected 99%
        BigDecimal expectedRTP = BigDecimal.valueOf(99.0);
        BigDecimal tolerance = BigDecimal.valueOf(1.0); // 1% tolerance
        
        assertTrue(rtp.compareTo(expectedRTP.subtract(tolerance)) >= 0 && 
                  rtp.compareTo(expectedRTP.add(tolerance)) <= 0,
                String.format("RTP %.2f%% should be within 1%% of expected 99.00%%", rtp));
    }

    private List<Player> createTestPlayers() {
        List<Player> players = new ArrayList<>();
        
        for (int i = 0; i < 100; i++) {
            PlayerRegistrationRequest request = new PlayerRegistrationRequest();
            request.setNickname("testPlayer" + i);
            request.setInitialBalance(BigDecimal.valueOf(1_000_000)); // Large balance for testing
            
            Player player = playerService.registerPlayer(request);
            players.add(player);
        }
        
        return players;
    }

    private RoundResult simulateRounds(List<Player> players, int roundCount, int threadId) {
        BigDecimal totalBets = BigDecimal.ZERO;
        BigDecimal totalWinnings = BigDecimal.ZERO;
        int betCount = 0;
        int winCount = 0;

        for (int round = 0; round < roundCount; round++) {
            // Create a new game round
            GameRound gameRound = gameService.createNewRound();
            
            // Each player places a bet on a random number
            for (Player player : players) {
                int betNumber = (round + player.getId().intValue()) % 10 + 1; // Distribute bets evenly
                
                BetRequest betRequest = new BetRequest();
                betRequest.setNickname(player.getNickname());
                betRequest.setBetNumber(betNumber);
                betRequest.setBetAmount(BET_AMOUNT);
                
                try {
                    gameService.placeBet(betRequest);
                    totalBets = totalBets.add(BET_AMOUNT);
                    betCount++;
                } catch (Exception e) {
                    // Player might have insufficient balance or other issues
                    // In real test, we'd handle this properly
                }
            }
            
            // Complete the round and calculate winnings
            var result = gameService.completeRound(gameRound);
            
            // Add up winnings from this round
            for (var winner : result.getWinners()) {
                totalWinnings = totalWinnings.add(winner.getWinnings());
                winCount++;
            }
            
            if (round % 10000 == 0 && round > 0) {
                log.debug("Thread {}: Completed {} rounds", threadId, round);
            }
        }

        log.info("Thread {} completed. Bets: {}, Wins: {}, Total bet: ${}, Total won: ${}", 
                threadId, betCount, winCount, totalBets, totalWinnings);
        
        return new RoundResult(totalBets, totalWinnings, betCount, winCount);
    }

    @BeforeEach
    void setUp() {
        // Clean up before each test if needed
    }

    private static class RoundResult {
        final BigDecimal totalBets;
        final BigDecimal totalWinnings;
        final int betCount;
        final int winCount;

        RoundResult(BigDecimal totalBets, BigDecimal totalWinnings, int betCount, int winCount) {
            this.totalBets = totalBets;
            this.totalWinnings = totalWinnings;
            this.betCount = betCount;
            this.winCount = winCount;
        }
    }
}
