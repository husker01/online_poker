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
    public ResponseEntity<?> deal() {
        try {
            Map<String, List<Card>> hands = gameService.dealCards(); // Adjusted to deal cards to seated players

            if (hands.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No players are seated."));
            }

            // Print each player's hand
            for (Map.Entry<String, List<Card>> entry : hands.entrySet()) {
                String playerName = entry.getKey();
                List<Card> playerHand = entry.getValue();

                System.out.println("Player: " + playerName + ", Hand: " + playerHand);
            }

            return ResponseEntity.ok(hands);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    @PostMapping("/restart")
    public ResponseEntity<?> restartGame() {
        gameService.restartGame();
        // Return some confirmation message or just an OK status
        return ResponseEntity.ok().body("Game has been restarted, hands cleared, deck renewed.");
    }


    @PostMapping("/join")
    public ResponseEntity<?> joinGame(@RequestBody PlayerJoinRequest request) {
        if (gameService.isPlayerNameTaken(request.getPlayerName())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Player name is already taken."
            ));
        }

        boolean success = gameService.takeSeat(request.getPlayerName(), request.getSeatNumber());
        if (!success) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Seat is already taken or invalid."
            ));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "playerName", request.getPlayerName(),
                "seatNumber", request.getSeatNumber()
        ));
    }


    @PostMapping("/leave")
    public ResponseEntity<?> leaveGame(@RequestBody Map<String, Integer> request) {
        int seatNumber = request.get("seatNumber");
        gameService.leaveSeat(seatNumber);
        return ResponseEntity.ok().body("Player has left seat " + seatNumber);
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
