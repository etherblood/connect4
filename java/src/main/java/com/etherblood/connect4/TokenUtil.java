package com.etherblood.connect4;

import static com.etherblood.connect4.Util.Long.toMask;

public class TokenUtil {

    public static final int WIDTH, HEIGHT, BUFFERED_HEIGHT;
    public static final long X_AXIS, Y_AXIS, FULL_BOARD;
    public static final int RIGHT, RIGHT_DOWN, RIGHT_UP, UP;

    static {
        WIDTH = 6;
        HEIGHT = 6;

        BUFFERED_HEIGHT = HEIGHT + 1;
        if (WIDTH * BUFFERED_HEIGHT > Long.SIZE) {
            throw new IllegalArgumentException();
        }
        Y_AXIS = toMask(HEIGHT);
        X_AXIS = toMask(WIDTH * BUFFERED_HEIGHT) / toMask(BUFFERED_HEIGHT);
        FULL_BOARD = X_AXIS * Y_AXIS;
        UP = 1;
        RIGHT = BUFFERED_HEIGHT;
        RIGHT_UP = RIGHT + UP;
        RIGHT_DOWN = RIGHT - UP;
    }

    public static long move(long tokens, long move) {
        return tokens | move;
    }

    public static boolean isGameOver(long ownTokens, long opponentTokens) {
        return isBoardFull(ownTokens, opponentTokens) || isWin(opponentTokens);
    }

    public static boolean isBoardFull(long ownTokens, long opponentTokens) {
        return occupied(ownTokens, opponentTokens) == FULL_BOARD;
    }

    public static boolean isWin(long tokens) {
        return squish(tokens, RIGHT_UP) != 0
                || squish(tokens, RIGHT_DOWN) != 0
                || squish(tokens, RIGHT) != 0
                || squish(tokens, UP) != 0;
    }

    public static long squish(long tokens, int directionShift) {
        tokens &= tokens << (2 * directionShift);
        tokens &= tokens << directionShift;
        return tokens;
    }

    public static long stretch(long tokens, int directionShift) {
        tokens |= tokens >>> (2 * directionShift);
        tokens |= tokens >>> directionShift;
        return tokens;
    }

    public static long generateMoves(long ownTokens, long opponentTokens) {
        return (occupied(ownTokens, opponentTokens) + X_AXIS) & FULL_BOARD;
    }

    public static long occupied(long ownTokens, long opponentTokens) {
        return ownTokens | opponentTokens;
    }

    public static long id(long ownTokens, long opponentTokens) {
        return (occupied(ownTokens, opponentTokens) + X_AXIS) ^ ownTokens;
    }
}
