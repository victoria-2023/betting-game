package com.bettinggame.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "players")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nickname cannot be blank")
    @Size(min = 3, max = 20, message = "Nickname must be between 3 and 20 characters")
    @Column(unique = true, nullable = false)
    private String nickname;
    
    @Column(nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(nullable = false)
    @Builder.Default
    private BigDecimal totalWinnings = BigDecimal.ZERO;
    
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore  // Prevent circular reference during JSON serialization
    private List<Bet> bets = new ArrayList<>();
    
    public void addWinnings(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.totalWinnings = this.totalWinnings.add(amount);
    }
    
    public void deductBet(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }
}
