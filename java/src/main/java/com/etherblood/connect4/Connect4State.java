package com.etherblood.connect4;

/**
 *
 * @author Philipp
 */
public interface Connect4State {

    int activePlayer();

    int opponent();

    long columnToTokenMove(int column);

    long tokenMoves();

    void tokenMove(long token);

    void tokenUndo(long token);

    boolean isBoardFull();

    boolean opponentWon();

    boolean isGameOver();

    long id();

    String asString();
}
