package com.bettinggame.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlayerRegistrationRequest {
    
    @NotBlank(message = "Nickname cannot be blank")
    @Size(min = 3, max = 20, message = "Nickname must be between 3 and 20 characters")
    private String nickname;
    
    private BigDecimal initialBalance = BigDecimal.valueOf(1000); // Default starting balance
}
