package com.etherblood.connect4;

import static com.etherblood.connect4.Util.Long.toFlag;
import static com.etherblood.connect4.Util.Long.toMask;

/**
 *
 * @author Philipp
 */
public class Connect4StateImpl implements Connect4State {

    public final int width, height, bufferedHeight;
    public final long xAxis, yAxis, fullBoard;
    public final int right, rightDown, rightUp, up;
    public long player0Tokens, player1Tokens;
    public boolean player1Active;

    public Connect4StateImpl() {
        this(7, 6);
    }

    public Connect4StateImpl(int width, int height) {
        this.width = width;
        this.height = height;
        bufferedHeight = height + 1;
        if (width * bufferedHeight - 1 > Long.SIZE) {
            throw new IllegalArgumentException();
        }
        player0Tokens = 0;
        player1Tokens = 0;
        player1Active = false;
        yAxis = toMask(height);
        xAxis = toMask(width * bufferedHeight) / toMask(bufferedHeight);
        fullBoard = xAxis * yAxis;
        up = 1;
        right = bufferedHeight;
        rightUp = right + up;
        rightDown = right - up;
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
    public long availableColumnMove(int column) {
        return (occupied() + xAxis) & columnMask(column);
    }

    private long columnMask(int column) {
        return yAxis << (column * bufferedHeight);
    }

    @Override
    public long availableMoves() {
        return (occupied() + xAxis) & fullBoard;
    }

    @Override
    public void move(long token) {
        if (player1Active) {
            player1Tokens ^= token;
            player1Active = false;
        } else {
            player0Tokens ^= token;
            player1Active = true;
        }
    }

    @Override
    public void unmove(long token) {
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
        if (player1Active) {
            opponentTokens = player0Tokens;
        } else {
            opponentTokens = player1Tokens;
        }
        return squishConnected(opponentTokens, rightUp) != 0
                && squishConnected(opponentTokens, rightDown) != 0
                && squishConnected(opponentTokens, right) != 0
                && squishConnected(opponentTokens, up) != 0;
    }

    private long squishConnected(long tokens, int directionShift) {
        tokens &= tokens << (2 * directionShift);
        tokens &= tokens << directionShift;
        return tokens;
    }

    @Override
    public boolean isGameOver() {
        return isBoardFull() || opponentWon();
    }

    @Override
    public long id() {
        return (occupied() + xAxis) ^ player0Tokens;
    }

    @Override
    public String asString() {
        String string = "";
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                long token = toFlag(y + x * bufferedHeight);
                if ((player0Tokens & token) != 0) {
                    string += "[0]";
                } else if ((player1Tokens & token) != 0) {
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
}
