package com.etherblood.connect4;

/**
 *
 * @author Philipp
 */
public interface Connect4State {
    
    static final int PLAYER_0 = 0;
    static final int PLAYER_1 = 1;

    int activePlayer();

    default int opponent() {
        return activePlayer() ^ PLAYER_0 ^ PLAYER_1;
    }

    default long availableColumnMove(int column) {
        return availableMoves() & columnMask(column);
    }
    
    long playerTokens(int player);

    long availableMoves();

    void move(long token);

    void unmove(long token);

    boolean isBoardFull();

    boolean opponentWon();

    default boolean isGameOver() {
        return isBoardFull() || opponentWon();
    }

    long id();
    
    default String asString() {
        String string = "";
        for (int y = height() - 1; y >= 0; y--) {
            for (int x = 0; x < width(); x++) {
                long token = columnMask(x) & rowMask(y);
                if ((playerTokens(PLAYER_0) & token) != 0) {
                    string += "[0]";
                } else if ((playerTokens(PLAYER_1) & token) != 0) {
                    string += "[1]";
                } else {
                    string += "[ ]";
                }
            }
            if (y != 0) {
                string += "\n";
            }
        }
        return string;
    }
    
    int width();
    
    int height();
    
    long rowMask(int row);
    
    long columnMask(int column);
}
