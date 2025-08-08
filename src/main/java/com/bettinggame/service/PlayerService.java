package com.bettinggame.service;

import com.bettinggame.dto.PlayerRegistrationRequest;
import com.bettinggame.model.Player;
import com.bettinggame.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {
    
    private final PlayerRepository playerRepository;
    
    @Transactional
    public Player registerPlayer(PlayerRegistrationRequest request) {
        log.info("Registering new player with nickname: {}", request.getNickname());
        
        if (playerRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("Nickname already exists: " + request.getNickname());
        }
        
        Player player = Player.builder()
                .nickname(request.getNickname())
                .balance(request.getInitialBalance())
                .build();
        
        Player savedPlayer = playerRepository.save(player);
        log.info("Player registered successfully: {}", savedPlayer.getNickname());
        return savedPlayer;
    }
    
    public Optional<Player> findByNickname(String nickname) {
        return playerRepository.findByNickname(nickname);
    }
    
    public Player getPlayerByNickname(String nickname) {
        return findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + nickname));
    }
    
    public List<Player> getTopWinners() {
        return playerRepository.findTopWinners();
    }
    
    @Transactional
    public Player updatePlayer(Player player) {
        return playerRepository.save(player);
    }
}
