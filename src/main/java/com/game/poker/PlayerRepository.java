package com.game.poker;


import com.game.poker.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer> {
    Optional<Player> findByPlayerName(String playerName);
    @Query("SELECT p FROM Player p WHERE p.seatNumber = (SELECT MIN(p.seatNumber) FROM Player p)")
    Optional<Player> findPlayerWithMinimumSeat();
}
