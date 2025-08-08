package com.bettinggame.repository;

import com.bettinggame.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    
    Optional<Player> findByNickname(String nickname);
    
    boolean existsByNickname(String nickname);
    
    @Query("SELECT p FROM Player p ORDER BY p.totalWinnings DESC")
    List<Player> findTopWinners();
}
