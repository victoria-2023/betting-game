package com.bettinggame.repository;

import com.bettinggame.model.Bet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BetRepository extends JpaRepository<Bet, Long> {
    
    List<Bet> findByGameRoundId(Long gameRoundId);
    
    List<Bet> findByPlayerIdOrderByPlacedAtDesc(Long playerId);
    
    @Query("SELECT b FROM Bet b WHERE b.gameRound.id = :gameRoundId AND b.player.id = :playerId")
    Optional<Bet> findByGameRoundIdAndPlayerId(@Param("gameRoundId") Long gameRoundId, 
                                               @Param("playerId") Long playerId);
    
    @Query("SELECT b FROM Bet b WHERE b.gameRound.id = :gameRoundId AND b.isWinner = true")
    List<Bet> findWinningBetsByGameRoundId(@Param("gameRoundId") Long gameRoundId);
}
