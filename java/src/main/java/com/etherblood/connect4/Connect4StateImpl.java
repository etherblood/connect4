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
    private long player0Tokens, player1Tokens;
    private boolean player1Active;

    public Connect4StateImpl() {
        this(7, 6);
    }

    public Connect4StateImpl(int width, int height) {
        this.width = width;
        this.height = height;
        bufferedHeight = height + 1;
        if (width * bufferedHeight > Long.SIZE) {
            throw new IllegalArgumentException();
        }
        player0Tokens = 0;
        player1Tokens = 0;
        player1Active = false;
        yAxis = toFlag(height) - 1;
        xAxis = (toFlag(width * bufferedHeight) - 1) / (toFlag(bufferedHeight) - 1);
        fullBoard = xAxis * yAxis;
        winShift0 = bufferedHeight;
        winShift1 = bufferedHeight - 1;
        winShift2 = bufferedHeight + 1;
        winShift3 = 1;
    }

    @Override
    public int activePlayer() {
        return player1Active ? 1 : 0;
    }

    @Override
    public int opponent() {
        return player1Active ? 0 : 1;
    }

    private long occupied() {
        return player0Tokens | player1Tokens;
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
        if (player1Active) {
            player1Tokens ^= token;
            player1Active = false;
        } else {
            player0Tokens ^= token;
            player1Active = true;
        }
    }

    @Override
    public void tokenUndo(long token) {
        if (player1Active) {
            player1Active = false;
            player0Tokens ^= token;
        } else {
            player1Active = true;
            player1Tokens ^= token;
        }
    }

    @Override
    public boolean isBoardFull() {
        return occupied() == fullBoard;
    }

    @Override
    public boolean opponentWon() {
        long opponentTokens;
        if(player1Active) {
            opponentTokens = player0Tokens;
        } else {
            opponentTokens = player1Tokens;
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
        return ((occupied() << 1) | xAxis) ^ player0Tokens;
    }

    @Override
    public String asString() {
        String string = "";
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                long token = toFlag(y + x * bufferedHeight);
                if ((player0Tokens & token) != 0) {
                    string += "[X]";
                } else if ((player1Tokens & token) != 0) {
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
