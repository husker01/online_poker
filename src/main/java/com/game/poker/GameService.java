package com.game.poker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.game.poker.Player;
import com.game.poker.PlayerRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GameService {


    @Autowired
    private PlayerRepository playerRepository;
    private Deck deck;
    private final Map<Integer, String> seats;
    private final Map<String, List<Card>> playerHands;
    private final Map<String, Integer> playerBalances;
    private Integer currentPlayerSeatNumber;
    private List<Card> communityCards = new ArrayList<>();


    private Map<Integer, Boolean> playerChecked = new ConcurrentHashMap<>(); // Maps seat number to check status

    public GameService() {
        this.deck = new Deck();
        this.deck.shuffle();
        this.seats = new ConcurrentHashMap<>();
        this.playerHands = new ConcurrentHashMap<>();
        this.playerBalances = new ConcurrentHashMap<>();
        this.communityCards = new ArrayList<>();
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

            // Convert hand to plain text
            String handText = hand.stream()
                    .map(card -> card.toString()) // Assuming Card has a toString method
                    .collect(Collectors.joining(", "));
            Player player = playerRepository.findByPlayerName(playerName)
                    .orElseThrow(() -> new RuntimeException("Player not found"));
            player.setPlayerHand(handText);
            playerRepository.save(player);
        });
        // Set the lowest seat number as the current player
        this.currentPlayerSeatNumber = seats.keySet().stream().min(Integer::compare).orElse(null);
        startBettingRound();
        return this.playerHands;
    }


    public boolean isPlayerNameTaken(String playerName) {
        return seats.values().stream().anyMatch(name -> name.equalsIgnoreCase(playerName));
    }


//    when all players checked, deal three community cards
    private boolean allPlayersChecked = false;
    public void startBettingRound() {
        playerChecked.clear();
        for (Integer seatNumber : seats.keySet()) {
            playerChecked.put(seatNumber, false);
        }
        // Other logic to start the betting round...
    }

    public void handleCheck(int seatNumber) {
        playerChecked.put(seatNumber, true); // Mark this player as checked
        updateCurrentPlayer();
        checkAllPlayersChecked();
        if (allPlayersChecked) {
            dealCommunityCards();
        }
    }



    private void updateCurrentPlayer() {
        // Assuming seats are numbered 1 to N and all are occupied
        int maxSeatNumber = Collections.max(seats.keySet());

        // Increment the current player seat number
        currentPlayerSeatNumber = (currentPlayerSeatNumber == null || currentPlayerSeatNumber >= maxSeatNumber) ? Collections.min(seats.keySet()) : currentPlayerSeatNumber + 1;

        // If the next seat is empty, find the next occupied seat
        while (!seats.containsKey(currentPlayerSeatNumber)) {
            currentPlayerSeatNumber = (currentPlayerSeatNumber >= maxSeatNumber) ? Collections.min(seats.keySet()) : currentPlayerSeatNumber + 1;
        }
    }

    public Integer getNextPlayerInfo() {
        return currentPlayerSeatNumber;
    }

    private void checkAllPlayersChecked() {
        allPlayersChecked = playerChecked.values().stream().allMatch(Boolean::booleanValue);
        if (allPlayersChecked) {
            // Reset for next round
            playerChecked.clear();
        }
    }

    public List<Card> getCommunityCards() {
        return communityCards;
    }
    public boolean getAllPlayersChecked() {
        return allPlayersChecked;
    }
    private void dealCommunityCards() {
        for (int i = 0; i < 3; i++) {
            communityCards.add(deck.deal());
        }
    }

    public void restartGame() {
        // Reinitialize the deck and shuffle
        this.deck = new Deck();
        this.deck.shuffle();

        // Clear all in-memory data structures
        seats.clear();
        playerHands.clear();

        // Delete all players from the database
        playerRepository.deleteAll();
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

            // Find player by name and delete
            playerRepository.findByPlayerName(playerName)
                    .ifPresent(playerRepository::delete);
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
