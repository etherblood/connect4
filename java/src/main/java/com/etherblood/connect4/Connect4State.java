package com.etherblood.connect4;

/**
 *
 * @author Philipp
 */
public interface Connect4State {

    int activePlayer();

    int opponent();

    long availableColumnMove(int column);

    long availableMoves();

    void move(long token);

    void unmove(long token);

    boolean isBoardFull();

    boolean opponentWon();

    boolean isGameOver();

    long id();
    
    String asString();
}
