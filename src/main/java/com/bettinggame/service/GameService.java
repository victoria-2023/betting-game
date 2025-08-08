package com.bettinggame.service;

import com.bettinggame.dto.BetRequest;
import com.bettinggame.dto.GameRoundResult;
import com.bettinggame.model.Bet;
import com.bettinggame.model.GameRound;
import com.bettinggame.model.Player;
import com.bettinggame.repository.BetRepository;
import com.bettinggame.repository.GameRoundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    
    private final GameRoundRepository gameRoundRepository;
    private final BetRepository betRepository;
    private final PlayerService playerService;
    private final Random random = new Random();
    
    private static final BigDecimal WIN_MULTIPLIER = BigDecimal.valueOf(9.9);
    
    @Transactional
    public GameRound createNewRound() {
        log.info("Creating new game round");
        
        GameRound gameRound = GameRound.builder()
                .status(GameRound.GameRoundStatus.BETTING_OPEN)
                .build();
        
        GameRound savedRound = gameRoundRepository.save(gameRound);
        log.info("New game round created with ID: {}", savedRound.getId());
        return savedRound;
    }
    
    public Optional<GameRound> getCurrentActiveRound() {
        return gameRoundRepository.findCurrentActiveRound();
    }
    
    @Transactional
    public Bet placeBet(BetRequest betRequest) {
        log.info("Placing bet for player: {} on number: {} with amount: {}", 
                betRequest.getNickname(), betRequest.getBetNumber(), betRequest.getBetAmount());
        
        Player player = playerService.getPlayerByNickname(betRequest.getNickname());
        GameRound currentRound = getCurrentActiveRound()
                .orElseThrow(() -> new IllegalStateException("No active game round available"));
        
        // Check if player has sufficient balance
        if (player.getBalance().compareTo(betRequest.getBetAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        
        // Check if player already placed a bet in this round
        Optional<Bet> existingBet = betRepository.findByGameRoundIdAndPlayerId(
                currentRound.getId(), player.getId());
        if (existingBet.isPresent()) {
            throw new IllegalArgumentException("Player already placed a bet in this round");
        }
        
        // Deduct bet amount from player balance
        player.deductBet(betRequest.getBetAmount());
        playerService.updatePlayer(player);
        
        Bet bet = Bet.builder()
                .player(player)
                .gameRound(currentRound)
                .betNumber(betRequest.getBetNumber())
                .betAmount(betRequest.getBetAmount())
                .build();
        
        Bet savedBet = betRepository.save(bet);
        log.info("Bet placed successfully with ID: {}", savedBet.getId());
        return savedBet;
    }
    
    @Transactional
    public GameRoundResult completeRound(GameRound gameRound) {
        log.info("Completing game round: {}", gameRound.getId());
        
        // Generate winning number
        int winningNumber = random.nextInt(10) + 1;
        gameRound.setWinningNumber(winningNumber);
        gameRound.setStatus(GameRound.GameRoundStatus.COMPLETED);
        gameRound.setEndTime(LocalDateTime.now());
        
        gameRoundRepository.save(gameRound);
        
        // Process winning bets
        List<Bet> allBets = betRepository.findByGameRoundId(gameRound.getId());
        List<Bet> winningBets = allBets.stream()
                .filter(bet -> bet.getBetNumber().equals(winningNumber))
                .toList();
        
        // Calculate and distribute winnings
        for (Bet winningBet : winningBets) {
            BigDecimal winnings = winningBet.getBetAmount().multiply(WIN_MULTIPLIER);
            winningBet.setWinnings(winnings);
            winningBet.setIsWinner(true);
            
            Player player = winningBet.getPlayer();
            player.addWinnings(winnings);
            playerService.updatePlayer(player);
            
            betRepository.save(winningBet);
        }
        
        log.info("Game round {} completed. Winning number: {}, Winners: {}", 
                gameRound.getId(), winningNumber, winningBets.size());
        
        // Create result DTO
        GameRoundResult.WinnerInfo[] winners = winningBets.stream()
                .map(bet -> GameRoundResult.WinnerInfo.builder()
                        .nickname(bet.getPlayer().getNickname())
                        .winnings(bet.getWinnings())
                        .build())
                .toArray(GameRoundResult.WinnerInfo[]::new);
        
        return GameRoundResult.builder()
                .roundId(gameRound.getId())
                .winningNumber(winningNumber)
                .endTime(gameRound.getEndTime())
                .winners(winners)
                .build();
    }
    
    @Transactional
    public void closeBetting(GameRound gameRound) {
        log.info("Closing betting for round: {}", gameRound.getId());
        gameRound.setStatus(GameRound.GameRoundStatus.BETTING_CLOSED);
        gameRoundRepository.save(gameRound);
    }
}
