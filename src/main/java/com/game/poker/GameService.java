package com.game.poker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.game.poker.Player;
import com.game.poker.PlayerRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {


    @Autowired
    private PlayerRepository playerRepository;
    private Deck deck;
    private final Map<Integer, String> seats;
    private final Map<String, List<Card>> playerHands;
    private final Map<String, Integer> playerBalances;
    public GameService() {
        this.deck = new Deck();
        this.deck.shuffle();
        this.seats = new ConcurrentHashMap<>();
        this.playerHands = new ConcurrentHashMap<>();
        this.playerBalances = new ConcurrentHashMap<>();
    }


    public Map<String, List<Card>> dealCards() {
        this.deck = new Deck();
        this.deck.shuffle();
        if (seats.isEmpty()) {
            throw new IllegalStateException("No players are seated.");
        }
        this.playerHands.clear(); // Clear previous hands
        this.seats.values().forEach(playerName -> {
            List<Card> hand = new ArrayList<>();
            hand.add(deck.deal());
            hand.add(deck.deal());
            this.playerHands.put(playerName, hand);
        });
        return this.playerHands;
    }

    public boolean isPlayerNameTaken(String playerName) {
        return seats.values().stream().anyMatch(name -> name.equalsIgnoreCase(playerName));
    }



    public void restartGame() {
        // Reinitialize the deck and shuffle
        this.deck = new Deck();
        this.deck.shuffle();

        // Clear hands without removing seated players
        for (String playerName : seats.values()) {
            playerHands.put(playerName, new ArrayList<>()); // Clear hands by assigning an empty list
        }
    }


    public boolean takeSeat(String playerName, int seatNumber, int buyInAmount) {
        if (seatNumber < 1 || seatNumber > 10 || seats.containsKey(seatNumber)) {
            return false; // Seat is invalid or already taken
        }
        // Create a new Player entity
        Player player = new Player();
        player.setPlayerName(playerName);
        player.setSeatNumber(seatNumber);
        player.setBuyInAmount(buyInAmount);
        player.setBalance(buyInAmount); // Assuming initial balance is same as buy-in amount
        // You might want to set 'playerHand' as well, depending on your requirements

        // Save the player to the database
        playerRepository.save(player);

        seats.put(seatNumber, playerName);
        playerBalances.put(playerName, buyInAmount);
        return true;
    }

    public void leaveSeat(int seatNumber) {
        if (seats.containsKey(seatNumber)) {
            String playerName = seats.remove(seatNumber);
            playerHands.remove(playerName); // Remove the hand of the player who left
        }
    }

    // Add a getter for player hands if you need to access it from outside
    public Map<String, List<Card>> getPlayerHands() {
        return playerHands;
    }

    // Add a getter for the seats if you need to access it from outside
    public Map<Integer, String> getSeats() {
        return seats;
    }

    // Method to get player hands with balances
    public Map<String, Object> getPlayerDetails() {
        Map<String, Object> details = new ConcurrentHashMap<>();

        for (Map.Entry<String, List<Card>> entry : playerHands.entrySet()) {
            String playerName = entry.getKey();
            List<Card> playerHand = entry.getValue();
            Integer playerBalance = playerBalances.getOrDefault(playerName, 0);

            Map<String, Object> playerDetails = new HashMap<>();
            playerDetails.put("hand", playerHand);
            playerDetails.put("balance", playerBalance);

            details.put(playerName, playerDetails);
        }

        return details;
    }



}
