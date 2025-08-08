package com.bettinggame.service;

import com.bettinggame.dto.BetRequest;
import com.bettinggame.model.Bet;
import com.bettinggame.model.GameRound;
import com.bettinggame.model.Player;
import com.bettinggame.repository.BetRepository;
import com.bettinggame.repository.GameRoundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRoundRepository gameRoundRepository;

    @Mock
    private BetRepository betRepository;

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private GameService gameService;

    private Player testPlayer;
    private GameRound testGameRound;
    private BetRequest betRequest;

    @BeforeEach
    void setUp() {
        testPlayer = Player.builder()
                .id(1L)
                .nickname("testPlayer")
                .balance(BigDecimal.valueOf(1000))
                .totalWinnings(BigDecimal.ZERO)
                .build();

        testGameRound = GameRound.builder()
                .id(1L)
                .status(GameRound.GameRoundStatus.BETTING_OPEN)
                .build();

        betRequest = new BetRequest();
        betRequest.setNickname("testPlayer");
        betRequest.setBetNumber(5);
        betRequest.setBetAmount(BigDecimal.valueOf(100));
    }

    @Test
    void createNewRound_Success() {
        // Given
        when(gameRoundRepository.save(any(GameRound.class))).thenReturn(testGameRound);

        // When
        GameRound result = gameService.createNewRound();

        // Then
        assertNotNull(result);
        assertEquals(GameRound.GameRoundStatus.BETTING_OPEN, result.getStatus());
        verify(gameRoundRepository).save(any(GameRound.class));
    }

    @Test
    void placeBet_Success() {
        // Given
        when(playerService.getPlayerByNickname("testPlayer")).thenReturn(testPlayer);
        when(gameRoundRepository.findCurrentActiveRound()).thenReturn(Optional.of(testGameRound));
        when(betRepository.findByGameRoundIdAndPlayerId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> {
            Bet bet = invocation.getArgument(0);
            bet.setId(1L);
            return bet;
        });

        // When
        Bet result = gameService.placeBet(betRequest);

        // Then
        assertNotNull(result);
        assertEquals(testPlayer, result.getPlayer());
        assertEquals(testGameRound, result.getGameRound());
        assertEquals(Integer.valueOf(5), result.getBetNumber());
        assertEquals(BigDecimal.valueOf(100), result.getBetAmount());
        
        verify(playerService).getPlayerByNickname("testPlayer");
        verify(playerService).updatePlayer(testPlayer);
        verify(betRepository).save(any(Bet.class));
    }

    @Test
    void placeBet_InsufficientBalance_ThrowsException() {
        // Given
        testPlayer.setBalance(BigDecimal.valueOf(50)); // Less than bet amount
        when(playerService.getPlayerByNickname("testPlayer")).thenReturn(testPlayer);
        when(gameRoundRepository.findCurrentActiveRound()).thenReturn(Optional.of(testGameRound));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.placeBet(betRequest)
        );

        assertEquals("Insufficient balance", exception.getMessage());
        verify(betRepository, never()).save(any(Bet.class));
    }

    @Test
    void placeBet_NoActiveRound_ThrowsException() {
        // Given
        when(playerService.getPlayerByNickname("testPlayer")).thenReturn(testPlayer);
        when(gameRoundRepository.findCurrentActiveRound()).thenReturn(Optional.empty());

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gameService.placeBet(betRequest)
        );

        assertEquals("No active game round available", exception.getMessage());
        verify(betRepository, never()).save(any(Bet.class));
    }

    @Test
    void placeBet_AlreadyPlacedBet_ThrowsException() {
        // Given
        Bet existingBet = Bet.builder().id(1L).build();
        when(playerService.getPlayerByNickname("testPlayer")).thenReturn(testPlayer);
        when(gameRoundRepository.findCurrentActiveRound()).thenReturn(Optional.of(testGameRound));
        when(betRepository.findByGameRoundIdAndPlayerId(anyLong(), anyLong()))
                .thenReturn(Optional.of(existingBet));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.placeBet(betRequest)
        );

        assertEquals("Player already placed a bet in this round", exception.getMessage());
        verify(betRepository, times(1)).findByGameRoundIdAndPlayerId(anyLong(), anyLong());
        verify(betRepository, never()).save(any(Bet.class));
    }
}
