package com.bettinggame.repository;

import com.bettinggame.model.GameRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRoundRepository extends JpaRepository<GameRound, Long> {
    
    @Query("SELECT gr FROM GameRound gr WHERE gr.status = 'BETTING_OPEN' ORDER BY gr.startTime DESC")
    Optional<GameRound> findCurrentActiveRound();
    
    @Query("SELECT gr FROM GameRound gr ORDER BY gr.startTime DESC")
    Optional<GameRound> findLatestRound();
}
