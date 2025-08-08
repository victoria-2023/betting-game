package com.bettinggame.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    @JsonIgnore  // Prevent circular reference during JSON serialization
    private Player player;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_round_id", nullable = false)
    private GameRound gameRound;
    
    @NotNull(message = "Bet number is required")
    @Min(value = 1, message = "Bet number must be between 1 and 10")
    @Max(value = 10, message = "Bet number must be between 1 and 10")
    @Column(nullable = false)
    private Integer betNumber;
    
    @NotNull(message = "Bet amount is required")
    @DecimalMin(value = "0.01", message = "Bet amount must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal betAmount;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal winnings;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean isWinner = false;
    
    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime placedAt = LocalDateTime.now();
}
