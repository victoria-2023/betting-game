package com.bettinggame.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetResponse {
    private Long id;
    private String playerNickname;
    private Integer betNumber;
    private BigDecimal betAmount;
    private Long gameRoundId;
    private LocalDateTime createdAt;
    private boolean isWinner;
    private BigDecimal winAmount;
}
