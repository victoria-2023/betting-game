package com.bettinggame.service;

import com.bettinggame.dto.GameRoundResult;
import com.bettinggame.dto.GameState;
import com.bettinggame.model.GameRound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameSchedulerService {
    
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;
    
    private static final long BETTING_DURATION_SECONDS = 10;
    private GameRound currentRound;
    private LocalDateTime roundStartTime;
    
    @Scheduled(fixedDelay = 1000) // Run every second
    public void manageGameRounds() {
        try {
            if (currentRound == null || 
                currentRound.getStatus() == GameRound.GameRoundStatus.COMPLETED) {
                startNewRound();
            } else {
                updateRoundProgress();
            }
        } catch (Exception e) {
            log.error("Error in game scheduler: ", e);
        }
    }
    
    private void startNewRound() {
        currentRound = gameService.createNewRound();
        roundStartTime = LocalDateTime.now();
        
        log.info("New round started: {}", currentRound.getId());
        
        // Broadcast new round start
        GameState gameState = GameState.builder()
                .currentRoundId(currentRound.getId())
                .timeRemaining(BETTING_DURATION_SECONDS)
                .bettingOpen(true)
                .phase("BETTING_OPEN")
                .build();
        
        messagingTemplate.convertAndSend("/topic/game-state", gameState);
    }
    
    private void updateRoundProgress() {
        long secondsElapsed = ChronoUnit.SECONDS.between(roundStartTime, LocalDateTime.now());
        long timeRemaining = BETTING_DURATION_SECONDS - secondsElapsed;
        
        if (timeRemaining <= 0 && currentRound.getStatus() == GameRound.GameRoundStatus.BETTING_OPEN) {
            // Close betting
            gameService.closeBetting(currentRound);
            
            GameState gameState = GameState.builder()
                    .currentRoundId(currentRound.getId())
                    .timeRemaining(0L)
                    .bettingOpen(false)
                    .phase("BETTING_CLOSED")
                    .build();
            
            messagingTemplate.convertAndSend("/topic/game-state", gameState);
            
            // Complete the round
            GameRoundResult result = gameService.completeRound(currentRound);
            
            // Broadcast results
            messagingTemplate.convertAndSend("/topic/round-results", result);
            
            // Update game state to completed
            gameState.setPhase("ROUND_COMPLETE");
            messagingTemplate.convertAndSend("/topic/game-state", gameState);
            
        } else if (currentRound.getStatus() == GameRound.GameRoundStatus.BETTING_OPEN) {
            // Update countdown
            GameState gameState = GameState.builder()
                    .currentRoundId(currentRound.getId())
                    .timeRemaining(timeRemaining)
                    .bettingOpen(true)
                    .phase("BETTING_OPEN")
                    .build();
            
            messagingTemplate.convertAndSend("/topic/game-state", gameState);
        }
    }
    
    public Optional<GameRound> getCurrentRound() {
        return Optional.ofNullable(currentRound);
    }
    
    public long getRemainingTime() {
        if (currentRound == null || roundStartTime == null) {
            return 0;
        }
        
        long secondsElapsed = ChronoUnit.SECONDS.between(roundStartTime, LocalDateTime.now());
        return Math.max(0, BETTING_DURATION_SECONDS - secondsElapsed);
    }
}
