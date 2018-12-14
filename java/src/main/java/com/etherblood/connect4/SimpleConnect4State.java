package com.etherblood.connect4;

import static com.etherblood.connect4.Util.toFlag;

/**
 *
 * @author Philipp
 */
public class SimpleConnect4State implements Connect4State {

    private final int width, height;
    private final long xAxis, yAxis, fullBoard;
    private final int[] winShifts;
    private final long[] winMasks;
    private long[] playerTokens;
    private int currentPlayer;

    public SimpleConnect4State() {
        this(7, 6);
    }

    public SimpleConnect4State(int width, int height) {
        this.width = width;
        this.height = height;
        playerTokens = new long[]{0, 0};
        currentPlayer = 0;
        fullBoard = toFlag(width * height) - 1;
        xAxis = toFlag(width) - 1;
        yAxis = fullBoard / xAxis;
        winShifts = new int[]{1, width - 1, width + 1, width};
        winMasks = new long[]{~yAxis, ~(xAxis | (yAxis << (width - 1))), ~(xAxis | yAxis), ~xAxis};
        for (int i = 0; i < 4; i++) {
            winMasks[i] &= winMasks[i] << winShifts[i];
        }
    }

    @Override
    public int currentPlayer() {
        return currentPlayer;
    }

    @Override
    public int opponent() {
        return currentPlayer ^ 1;
    }

    private long occupied() {
        return playerTokens[0] | playerTokens[1];
    }

    @Override
    public long columnToTokenMove(int column) {
        return tokenMoves() & (yAxis << column);
    }

    @Override
    public long tokenMoves() {
        long tokens = occupied();
        return ~(tokens | ~tokens << width) & fullBoard;
    }

    @Override
    public void tokenMove(long token) {
        playerTokens[currentPlayer] ^= token;
        currentPlayer = opponent();
    }

    @Override
    public void tokenUndo(long token) {
        currentPlayer = opponent();
        playerTokens[currentPlayer] ^= token;
    }

    @Override
    public boolean isBoardFull() {
        return occupied() == fullBoard;
    }

    @Override
    public boolean opponentWon() {
        long tokens = playerTokens[opponent()];
        for (int i = 0; i < 4; i++) {
            long candidates = tokens;
            candidates &= candidates << (2 * winShifts[i]);
            candidates &= winMasks[i];
            candidates &= candidates << winShifts[i];
            if (candidates != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isGameOver() {
        return isBoardFull() || opponentWon();
    }

    @Override
    public long id() {
        return ((occupied() << width) | xAxis) ^ playerTokens[0];
    }

    @Override
    public String asString() {
        String string = "";
        for (int y = height - 1; y < height; y--) {
            for (int x = 0; x < width; x++) {
                long token = toFlag(x + y * width);
                if ((playerTokens[0] & token) != 0) {
                    string += "[X]";
                } else if ((playerTokens[1] & token) != 0) {
                    string += "[O]";
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
}
