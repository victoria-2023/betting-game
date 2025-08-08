package com.bettinggame.service;

import com.bettinggame.dto.PlayerRegistrationRequest;
import com.bettinggame.model.Player;
import com.bettinggame.repository.PlayerRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private PlayerRegistrationRequest registrationRequest;
    private Player testPlayer;

    @BeforeEach
    void setUp() {
        registrationRequest = new PlayerRegistrationRequest();
        registrationRequest.setNickname("testPlayer");
        registrationRequest.setInitialBalance(BigDecimal.valueOf(1000));

        testPlayer = Player.builder()
                .id(1L)
                .nickname("testPlayer")
                .balance(BigDecimal.valueOf(1000))
                .totalWinnings(BigDecimal.ZERO)
                .build();
    }

    @Test
    void registerPlayer_Success() {
        // Given
        when(playerRepository.existsByNickname(anyString())).thenReturn(false);
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        // When
        Player result = playerService.registerPlayer(registrationRequest);

        // Then
        assertNotNull(result);
        assertEquals("testPlayer", result.getNickname());
        assertEquals(BigDecimal.valueOf(1000), result.getBalance());
        verify(playerRepository).existsByNickname("testPlayer");
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void registerPlayer_NicknameExists_ThrowsException() {
        // Given
        when(playerRepository.existsByNickname(anyString())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.registerPlayer(registrationRequest)
        );

        assertEquals("Nickname already exists: testPlayer", exception.getMessage());
        verify(playerRepository).existsByNickname("testPlayer");
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void findByNickname_PlayerExists_ReturnsPlayer() {
        // Given
        when(playerRepository.findByNickname("testPlayer")).thenReturn(Optional.of(testPlayer));

        // When
        Optional<Player> result = playerService.findByNickname("testPlayer");

        // Then
        assertTrue(result.isPresent());
        assertEquals("testPlayer", result.get().getNickname());
        verify(playerRepository).findByNickname("testPlayer");
    }

    @Test
    void getPlayerByNickname_PlayerNotFound_ThrowsException() {
        // Given
        when(playerRepository.findByNickname("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.getPlayerByNickname("nonexistent")
        );

        assertEquals("Player not found: nonexistent", exception.getMessage());
        verify(playerRepository).findByNickname("nonexistent");
    }
}
