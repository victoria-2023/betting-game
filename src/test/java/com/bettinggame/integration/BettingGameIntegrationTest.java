package com.bettinggame.integration;

import com.bettinggame.dto.BetRequest;
import com.bettinggame.dto.PlayerRegistrationRequest;
import com.bettinggame.repository.PlayerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BettingGameIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlayerRepository playerRepository;

    @BeforeEach
    void setUp() {
        playerRepository.deleteAll();
    }

    @Test
    void fullGameFlow_RegisterPlayerAndPlaceBet() throws Exception {
        // Step 1: Register a player
        PlayerRegistrationRequest registrationRequest = new PlayerRegistrationRequest();
        registrationRequest.setNickname("testPlayer");
        registrationRequest.setInitialBalance(BigDecimal.valueOf(2000));

        mockMvc.perform(post("/api/players/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("testPlayer"))
                .andExpect(jsonPath("$.balance").value(2000));

        // Step 2: Verify player is in database
        mockMvc.perform(get("/api/players/testPlayer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("testPlayer"))
                .andExpect(jsonPath("$.balance").value(2000));

        // Step 3: Place a bet (Note: this might fail if no active game round)
        BetRequest betRequest = new BetRequest();
        betRequest.setNickname("testPlayer");
        betRequest.setBetAmount(BigDecimal.valueOf(100));
        betRequest.setBetNumber(5);

        // This test may fail if no active round, but we're testing the integration
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void registerPlayer_DuplicateNickname_ReturnsBadRequest() throws Exception {
        // Register first player
        PlayerRegistrationRequest registrationRequest = new PlayerRegistrationRequest();
        registrationRequest.setNickname("duplicateTest");
        registrationRequest.setInitialBalance(BigDecimal.valueOf(1000));

        mockMvc.perform(post("/api/players/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk());

        // Try to register second player with same nickname
        mockMvc.perform(post("/api/players/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isBadRequest());
    }
}
