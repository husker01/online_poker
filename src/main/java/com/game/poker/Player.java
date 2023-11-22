package com.game.poker;

import jakarta.persistence.*;

@Entity
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "player_name")
    private String playerName;

    @Column(name = "buy_in_amount")
    private Integer buyInAmount;

    @Column(name = "seat_number")
    private Integer seatNumber;

    @Column(name = "player_hand")
    private String playerHand; // Stored as a JSON or delimited string

    @Column(name = "balance")
    private Integer balance;

    // Constructor
    public Player() {
        // Default constructor if needed
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Integer getBuyInAmount() {
        return buyInAmount;
    }

    public void setBuyInAmount(Integer buyInAmount) {
        this.buyInAmount = buyInAmount;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getPlayerHand() {
        return playerHand;
    }

    public void setPlayerHand(String playerHand) {
        this.playerHand = playerHand;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    // Additional methods if required
}
