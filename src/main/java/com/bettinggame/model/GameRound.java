package com.bettinggame.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_rounds")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRound {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private Integer winningNumber;
    
    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime startTime = LocalDateTime.now();
    
    @Column
    private LocalDateTime endTime;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameRoundStatus status = GameRoundStatus.BETTING_OPEN;
    
    @OneToMany(mappedBy = "gameRound", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Bet> bets = new ArrayList<>();
    
    public enum GameRoundStatus {
        BETTING_OPEN,
        BETTING_CLOSED,
        COMPLETED
    }
}
