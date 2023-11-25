package com.game.poker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/poker")
public class PokerController {

    private final GameService gameService;
    @Autowired
    private final PlayerRepository playerRepository;
    public PokerController(GameService gameService, PlayerRepository playerRepository) {
        this.gameService = gameService;
        this.playerRepository = playerRepository;
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

        // Use the new takeSeat method that also takes the buyInAmount
        boolean success = gameService.takeSeat(request.getPlayerName(), request.getSeatNumber(), request.getBuyInAmount());
        if (!success) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Seat is already taken or invalid."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "playerName", request.getPlayerName(),
                "seatNumber", request.getSeatNumber(),
                "buyInAmount", request.getBuyInAmount() // Include the buy-in amount in the response
        ));
    }



    @PostMapping("/leave")
    public ResponseEntity<?> leaveGame(@RequestBody Map<String, Integer> request) {
        int seatNumber = request.get("seatNumber");
        gameService.leaveSeat(seatNumber);
        return ResponseEntity.ok().build(); // Or return a more detailed response if needed
    }

    @GetMapping("/minimum-seat")
    public ResponseEntity<?> getMinimumSeatPlayer() {
        return playerRepository.findPlayerWithMinimumSeat()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }





//    @PostMapping("/bet")
//    public ResponseEntity<?> handleBet(@RequestBody BetRequest request) {
//        gameService.handleBet(request.getSeatNumber(), request.getBetAmount());
//        // Return a response indicating the next player's turn
//        return ResponseEntity.ok(gameService.getNextPlayerInfo());
//    }

    @PostMapping("/check")
    public ResponseEntity<?> handleCheck(@RequestBody CheckRequest request) {
        gameService.handleCheck(request.getSeatNumber());
        Integer nextPlayerSeatNumber = gameService.getNextPlayerInfo();
        boolean shouldDisplayCommunityCards = gameService.getAllPlayersChecked(); // Assuming this getter method exists

        return ResponseEntity.ok(Map.of(
                "nextPlayerSeatNumber", nextPlayerSeatNumber,
                "displayCommunityCards", shouldDisplayCommunityCards
        ));
    }

    @GetMapping("/communityCards")
    public ResponseEntity<List<Card>> getCommunityCards() {
        List<Card> communityCards = gameService.getCommunityCards();
        return ResponseEntity.ok(communityCards);
    }

    // Inner classes for request payloads
    public static class BetRequest {
        private int seatNumber;
        private int betAmount;
        // Getters and setters...
    }

    public static class CheckRequest {
        private int seatNumber;
        public int getSeatNumber() {
            return seatNumber;
        }
        // Getters and setters...
    }














    // Helper class to handle the join request
    public static class PlayerJoinRequest {
        private String playerName;
        private int seatNumber;
        private int buyInAmount; // New field for buy-in amount
        // Default constructor is needed for JSON deserialization
        public PlayerJoinRequest() {
        }
        public int getBuyInAmount() {
            return buyInAmount;
        }
        public void setBuyInAmount(int buyInAmount) {
            this.buyInAmount = buyInAmount;
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
