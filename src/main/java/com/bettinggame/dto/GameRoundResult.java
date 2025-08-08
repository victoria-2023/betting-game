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
public class GameRoundResult {
    
    private Long roundId;
    private Integer winningNumber;
    private LocalDateTime endTime;
    private WinnerInfo[] winners;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WinnerInfo {
        private String nickname;
        private BigDecimal winnings;
    }
}
