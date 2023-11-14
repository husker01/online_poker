package com.game.poker;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    private Deck deck;
    private final Map<Integer, String> seats;
    private final Map<String, List<Card>> playerHands;

    public GameService() {
        this.deck = new Deck();
        this.deck.shuffle();
        this.seats = new ConcurrentHashMap<>();
        this.playerHands = new ConcurrentHashMap<>();
    }

    public Map<String, List<Card>> dealCards() {
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

    public void restartGame() {
        // Reinitialize the deck and shuffle
        this.deck = new Deck();
        this.deck.shuffle();

        // Clear hands without removing seated players
        for (String playerName : seats.values()) {
            playerHands.put(playerName, new ArrayList<>()); // Clear hands by assigning an empty list
        }
    }


    public boolean takeSeat(String playerName, int seatNumber) {
        if (seatNumber < 1 || seatNumber > 10 || seats.containsKey(seatNumber)) {
            return false; // Seat is invalid or already taken
        }
        seats.put(seatNumber, playerName);
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
}
