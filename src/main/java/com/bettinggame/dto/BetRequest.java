package com.bettinggame.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BetRequest {
    
    @NotNull(message = "Player nickname is required")
    private String nickname;
    
    @NotNull(message = "Bet number is required")
    @Min(value = 1, message = "Bet number must be between 1 and 10")
    @Max(value = 10, message = "Bet number must be between 1 and 10")
    private Integer betNumber;
    
    @NotNull(message = "Bet amount is required")
    @DecimalMin(value = "0.01", message = "Bet amount must be greater than 0")
    private BigDecimal betAmount;
}
