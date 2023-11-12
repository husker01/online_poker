package com.game.poker;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/poker")
public class PokerController {

    private final GameService gameService;

    public PokerController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/deal")
    public ResponseEntity<List<List<Card>>> deal() {
        List<List<Card>> hands = gameService.dealCards(10); // Example for 10 players
        return ResponseEntity.ok(hands);
    }
    @PostMapping("/restart")
    public ResponseEntity<?> restartGame() {
        gameService.restartGame();
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/start")
    public ResponseEntity<?> startGame(@RequestParam("players") int numPlayers) {
        if (numPlayers < 2 || numPlayers > 10) {
            return ResponseEntity.badRequest().body("Number of players must be between 2 and 10.");
        }
        List<List<Card>> playersHands = gameService.startGame(numPlayers);
        return ResponseEntity.ok(playersHands);
    }
    @PostMapping("/join")
    public ResponseEntity<?> joinGame(@RequestBody PlayerJoinRequest request) {
        boolean success = gameService.takeSeat(request.getPlayerName(), request.getSeatNumber());
        if (!success) {
            return ResponseEntity.badRequest().body("Seat is already taken or invalid.");
        }
        return ResponseEntity.ok(Map.of(
                "playerName", request.getPlayerName(),
                "seatNumber", request.getSeatNumber()
        ));
    }



    // Helper class to handle the join request
    public static class PlayerJoinRequest {
        private String playerName;
        private int seatNumber;

        // Default constructor is needed for JSON deserialization
        public PlayerJoinRequest() {
        }

        // Getters and setters are required for accessing private fields
        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }

        public int getSeatNumber() {
            return seatNumber;
        }

        public void setSeatNumber(int seatNumber) {
            this.seatNumber = seatNumber;
        }
    }


}
