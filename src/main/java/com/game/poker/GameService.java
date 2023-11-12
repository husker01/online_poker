package com.game.poker;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    private Deck deck = new Deck();

    private final Map<Integer, String> seats = new ConcurrentHashMap<>();
    public GameService() {
        deck.shuffle();
    }

    public List<List<Card>> dealCards(int numberOfPlayers) {
        List<List<Card>> playerHands = new ArrayList<>();
        for (int i = 0; i < numberOfPlayers; i++) {
            List<Card> hand = new ArrayList<>();
            hand.add(deck.deal());
            hand.add(deck.deal());
            playerHands.add(hand);
        }
        return playerHands;
    }
    public void restartGame() {
        // Logic to restart the game
        // This could involve reinitializing the deck, clearing player hands, etc.
        this.deck = new Deck();
        deck.shuffle();
    }
    public List<List<Card>> startGame(int numPlayers) {
        restartGame(); // Reset the game
        return dealCards(numPlayers); // Deal cards to the given number of players
    }
    public boolean takeSeat(String playerName, int seatNumber) {
        if (seatNumber < 1 || seatNumber > 10 || seats.containsKey(seatNumber)) {
            return false; // Seat is invalid or already taken
        }
        seats.put(seatNumber, playerName);
        return true;
    }
}
