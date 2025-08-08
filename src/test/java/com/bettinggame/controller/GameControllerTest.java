package com.bettinggame.controller;

import com.bettinggame.dto.BetRequest;
import com.bettinggame.dto.PlayerRegistrationRequest;
import com.bettinggame.model.Bet;
import com.bettinggame.model.GameRound;
import com.bettinggame.model.Player;
import com.bettinggame.service.GameSchedulerService;
import com.bettinggame.service.GameService;
import com.bettinggame.service.PlayerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private GameService gameService;

    @MockBean
    private GameSchedulerService gameSchedulerService;

    @Autowired
    private ObjectMapper objectMapper;

    private Player testPlayer;
    private PlayerRegistrationRequest registrationRequest;
    private BetRequest betRequest;
    private Bet testBet;

    @BeforeEach
    void setUp() {
        testPlayer = Player.builder()
                .id(1L)
                .nickname("testPlayer")
                .balance(BigDecimal.valueOf(1000))
                .totalWinnings(BigDecimal.ZERO)
                .build();

        registrationRequest = new PlayerRegistrationRequest();
        registrationRequest.setNickname("testPlayer");
        registrationRequest.setInitialBalance(BigDecimal.valueOf(1000));

        betRequest = new BetRequest();
        betRequest.setNickname("testPlayer");
        betRequest.setBetNumber(5);
        betRequest.setBetAmount(BigDecimal.valueOf(100));

        testBet = Bet.builder()
                .id(1L)
                .player(testPlayer)
                .betNumber(5)
                .betAmount(BigDecimal.valueOf(100))
                .build();
    }

    @Test
    void registerPlayer_Success() throws Exception {
        // Given
        when(playerService.registerPlayer(any(PlayerRegistrationRequest.class)))
                .thenReturn(testPlayer);

        // When & Then
        mockMvc.perform(post("/api/players/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("testPlayer"))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void registerPlayer_InvalidNickname_ReturnsBadRequest() throws Exception {
        // Given
        registrationRequest.setNickname("ab"); // Too short

        // When & Then
        mockMvc.perform(post("/api/players/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPlayer_Found() throws Exception {
        // Given
        when(playerService.findByNickname("testPlayer")).thenReturn(Optional.of(testPlayer));

        // When & Then
        mockMvc.perform(get("/api/players/testPlayer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("testPlayer"));
    }

    @Test
    void getPlayer_NotFound() throws Exception {
        // Given
        when(playerService.findByNickname("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/players/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTopWinners_Success() throws Exception {
        // Given
        List<Player> winners = Arrays.asList(testPlayer);
        when(playerService.getTopWinners()).thenReturn(winners);

        // When & Then
        mockMvc.perform(get("/api/players/winners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nickname").value("testPlayer"));
    }

    @Test
    void placeBet_Success() throws Exception {
        // Given
        when(gameService.placeBet(any(BetRequest.class))).thenReturn(testBet);

        // When & Then
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.betNumber").value(5))
                .andExpect(jsonPath("$.betAmount").value(100));
    }

    @Test
    void placeBet_InvalidBetNumber_ReturnsBadRequest() throws Exception {
        // Given
        betRequest.setBetNumber(11); // Invalid bet number

        // When & Then
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGameState_Success() throws Exception {
        // Given
        GameRound testRound = GameRound.builder()
                .id(1L)
                .status(GameRound.GameRoundStatus.BETTING_OPEN)
                .build();
        when(gameSchedulerService.getCurrentRound()).thenReturn(Optional.of(testRound));
        when(gameSchedulerService.getRemainingTime()).thenReturn(8L);

        // When & Then
        mockMvc.perform(get("/api/game/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRoundId").value(1))
                .andExpect(jsonPath("$.timeRemaining").value(8))
                .andExpect(jsonPath("$.bettingOpen").value(true));
    }
}
