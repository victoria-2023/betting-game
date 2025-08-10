package com.bettinggame.controller;

import com.bettinggame.dto.BetRequest;
import com.bettinggame.dto.BetResponse;
import com.bettinggame.dto.GameState;
import com.bettinggame.dto.PlayerRegistrationRequest;
import com.bettinggame.model.Bet;
import com.bettinggame.model.GameRound;
import com.bettinggame.model.Player;
import com.bettinggame.service.GameSchedulerService;
import com.bettinggame.service.GameService;
import com.bettinggame.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GameController {
    
    private final PlayerService playerService;
    private final GameService gameService;
    private final GameSchedulerService gameSchedulerService;
    
    @PostMapping("/players/register")
    public ResponseEntity<Player> registerPlayer(@Valid @RequestBody PlayerRegistrationRequest request) {
        try {
            Player player = playerService.registerPlayer(request);
            return ResponseEntity.ok(player);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/players/{nickname}")
    public ResponseEntity<Player> getPlayer(@PathVariable String nickname) {
        Optional<Player> player = playerService.findByNickname(nickname);
        return player.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/players/winners")
    public ResponseEntity<List<Player>> getTopWinners() {
        List<Player> winners = playerService.getTopWinners();
        return ResponseEntity.ok(winners);
    }
    
    @PostMapping("/bets")
    public ResponseEntity<?> placeBet(@Valid @RequestBody BetRequest betRequest) {
        try {
            Bet bet = gameService.placeBet(betRequest);
            
            // Convert to DTO to avoid circular reference
            BetResponse response = BetResponse.builder()
                    .id(bet.getId())
                    .playerNickname(bet.getPlayer().getNickname())
                    .betNumber(bet.getBetNumber())
                    .betAmount(bet.getBetAmount())
                    .gameRoundId(bet.getGameRound().getId())
                    .createdAt(bet.getPlacedAt())
                    .isWinner(bet.getIsWinner())
                    .winAmount(bet.getWinnings() != null ? bet.getWinnings() : BigDecimal.ZERO)
                    .build();
                    
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error placing bet: ", e);
            Map<String, String> errorResponse = Map.of("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/game/state")
    public ResponseEntity<GameState> getGameState() {
        Optional<GameRound> currentRound = gameSchedulerService.getCurrentRound();
        
        if (currentRound.isEmpty()) {
            return ResponseEntity.ok(GameState.builder()
                    .bettingOpen(false)
                    .timeRemaining(0L)
                    .phase("NO_ACTIVE_ROUND")
                    .build());
        }
        
        GameRound round = currentRound.get();
        long timeRemaining = gameSchedulerService.getRemainingTime();
        
        GameState gameState = GameState.builder()
                .currentRoundId(round.getId())
                .timeRemaining(timeRemaining)
                .bettingOpen(round.getStatus() == GameRound.GameRoundStatus.BETTING_OPEN)
                .phase(round.getStatus().name())
                .build();
        
        return ResponseEntity.ok(gameState);
    }
    
    // WebSocket message handlers
    @MessageMapping("/place-bet")
    @SendTo("/topic/bet-placed")
    public Bet handleBetPlacement(@Valid BetRequest betRequest) {
        return gameService.placeBet(betRequest);
    }
}
