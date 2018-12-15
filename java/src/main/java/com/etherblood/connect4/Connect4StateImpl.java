package com.etherblood.connect4;

import static com.etherblood.connect4.Util.toFlag;

/**
 *
 * @author Philipp
 */
public class Connect4StateImpl implements Connect4State {

    private final int width, height, bufferedHeight;
    private final long xAxis, yAxis, fullBoard;
    private final int winShift0, winShift1, winShift2, winShift3;
    private long tokens0, tokens1;
    private int currentPlayer;

    public Connect4StateImpl() {
        this(7, 6);
    }

    public Connect4StateImpl(int width, int height) {
        this.width = width;
        this.height = height;
        bufferedHeight = height + 1;
        if(width * bufferedHeight > Long.SIZE) {
            throw new IllegalArgumentException();
        }
        tokens0 = 0;
        tokens1 = 0;
        currentPlayer = 0;
        yAxis = toFlag(height) - 1;
        xAxis = (toFlag(width * bufferedHeight) - 1) / (toFlag(bufferedHeight) - 1);
        fullBoard = xAxis * yAxis;
        winShift0 = bufferedHeight;
        winShift1 = bufferedHeight - 1;
        winShift2 = bufferedHeight + 1;
        winShift3 = 1;
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
        return (occupied() + xAxis) & columnMask(column);
    }

    private long columnMask(int column) {
        return yAxis << (column * bufferedHeight);
    }

    @Override
    public long tokenMoves() {
        return (occupied() + xAxis) & fullBoard;
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
        return shiftWins(opponentTokens, winShift0) != 0
                && shiftWins(opponentTokens, winShift1) != 0
                && shiftWins(opponentTokens, winShift2) != 0
                && shiftWins(opponentTokens, winShift3) != 0;
    }

    private long shiftWins(long tokens, int winShift) {
        tokens &= tokens << (2 * winShift);
        tokens &= tokens << winShift;
        return tokens;
    }

    @Override
    public boolean isGameOver() {
        return isBoardFull() || opponentWon();
    }

    @Override
    public long id() {
        return ((occupied() << 1) | xAxis) ^ tokens0;
    }

    @Override
    public String asString() {
        String string = "";
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                long token = toFlag(y + x * bufferedHeight);
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
