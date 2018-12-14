package com.etherblood.connect4;

import static com.etherblood.connect4.Util.toFlag;

/**
 *
 * @author Philipp
 */
public class FastConnect4State implements Connect4State {

    private final int width, height;
    private final long xAxis, yAxis, fullBoard;
    private final int winShift0, winShift1, winShift2, winShift3;
    private final long winMask0, winMask1, winMask2, winMask3;
    private long tokens0, tokens1;
    private int currentPlayer;

    public FastConnect4State() {
        this(7, 6);
    }

    public FastConnect4State(int width, int height) {
        this.width = width;
        this.height = height;
        tokens0 = 0;
        tokens1 = 0;
        currentPlayer = 0;
        fullBoard = toFlag(width * height) - 1;
        xAxis = toFlag(width) - 1;
        yAxis = fullBoard / xAxis;
        winShift0 = 1;
        winShift1 = width - 1;
        winShift2 = width + 1;
        winShift3 = width;
        long baseMask;
        baseMask = ~yAxis;
        winMask0 = baseMask & (baseMask << winShift0);
        baseMask = ~(xAxis | (yAxis << (width - 1)));
        winMask1 = baseMask & (baseMask << winShift1);
        baseMask = ~(xAxis | yAxis);
        winMask2 = baseMask & (baseMask << winShift2);
        baseMask = ~xAxis;
        winMask3 = baseMask & (baseMask << winShift3);
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
        return tokens0 | tokens1;
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
        switch (currentPlayer) {
            case 0:
                tokens0 ^= token;
                break;
            case 1:
                tokens1 ^= token;
                break;
            default:
                throw new AssertionError();
        }
        currentPlayer = opponent();
    }

    @Override
    public void tokenUndo(long token) {
        currentPlayer = opponent();
        switch (currentPlayer) {
            case 0:
                tokens0 ^= token;
                break;
            case 1:
                tokens1 ^= token;
                break;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public boolean isBoardFull() {
        return occupied() == fullBoard;
    }

    @Override
    public boolean opponentWon() {
        long opponentTokens;
        switch (currentPlayer) {
            case 0:
                opponentTokens = tokens1;
                break;
            case 1:
                opponentTokens = tokens0;
                break;
            default:
                throw new AssertionError();
        }
        return winHelper(opponentTokens, winMask0, winShift0) != 0
                && winHelper(opponentTokens, winMask1, winShift1) != 0
                && winHelper(opponentTokens, winMask2, winShift2) != 0
                && winHelper(opponentTokens, winMask3, winShift3) != 0;
    }

    private long winHelper(long tokens, long winMask, int winShift) {
        tokens &= tokens << (2 * winShift);
        tokens &= winMask;
        tokens &= tokens << winShift;
        return tokens;
    }

    @Override
    public boolean isGameOver() {
        return isBoardFull() || opponentWon();
    }

    @Override
    public long id() {
        return ((occupied() << width) | xAxis) ^ tokens0;
    }

    @Override
    public String asString() {
        String string = "";
        for (int y = height - 1; y < height; y--) {
            for (int x = 0; x < width; x++) {
                long token = toFlag(x + y * width);
                if ((tokens0 & token) != 0) {
                    string += "[X]";
                } else if ((tokens1 & token) != 0) {
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
