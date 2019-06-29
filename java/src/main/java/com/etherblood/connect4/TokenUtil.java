package com.etherblood.connect4;

import static com.etherblood.connect4.Util.Long.toMask;

public class TokenUtil {

    public static final int WIDTH, HEIGHT, BUFFERED_HEIGHT;
    public static final long X_AXIS, Y_AXIS, FULL_BOARD, LEFT_SIDE, CENTER_COLUMN;
    public static final int RIGHT, RIGHT_DOWN, RIGHT_UP, UP;

    public static final long[] WIN_CHECK_PATTERNS;

    static {
        WIDTH = 6;
        HEIGHT = 6;

        BUFFERED_HEIGHT = HEIGHT + 1;
        if (WIDTH * BUFFERED_HEIGHT > Long.SIZE) {
            throw new IllegalArgumentException();
        }
        Y_AXIS = toMask(HEIGHT);
        X_AXIS = toMask(WIDTH * BUFFERED_HEIGHT) / toMask(BUFFERED_HEIGHT);
        UP = 1;
        RIGHT = BUFFERED_HEIGHT;
        RIGHT_UP = RIGHT + UP;
        RIGHT_DOWN = RIGHT - UP;
        FULL_BOARD = X_AXIS * Y_AXIS;
        LEFT_SIDE = FULL_BOARD >>> (WIDTH / 2 * RIGHT);
        CENTER_COLUMN = (WIDTH & 1) != 0 ? Y_AXIS << (WIDTH / 2 * RIGHT) : 0;

        WIN_CHECK_PATTERNS = new long[4];
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                WIN_CHECK_PATTERNS[(x + 2 * y) & 3] |= Util.Long.toFlag(index(x, y));
            }
        }
    }

    public static boolean isSymmetrical(long tokens) {
        for (int x = 0; x < WIDTH / 2; x++) {
            long left = tokens >>> (x * RIGHT);
            long right = tokens >>> ((WIDTH - x - 1) * RIGHT);
            if (((left ^ right) & Y_AXIS) != 0) {
                return false;
            }
        }
        return true;
    }
    
    public static long mirror(long tokens) {
        long result = tokens & CENTER_COLUMN;
        for (int x = 0; x < WIDTH / 2; x++) {
            int mirrorX = WIDTH - x - 1;
            int offset = mirrorX - x;
            
            result |= (tokens & (Y_AXIS << (x * RIGHT))) << (offset * RIGHT);
            result |= (tokens & (Y_AXIS << (mirrorX * RIGHT))) >>> (offset * RIGHT);
        }
        return result;
    }

    public static long move(long tokens, long moves) {
        return tokens | moves;
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

    public static String toString(long mask) {
        return toString(mask, 0);
    }

    public static String toString(long a, long b) {
        StringBuilder builder = new StringBuilder();
        for (int y = HEIGHT - 1; y >= 0; y--) {
            for (int x = 0; x < WIDTH; x++) {
                if ((Util.Long.toFlag(index(x, y)) & a) != 0) {
                    builder.append("[x]");
                } else if ((Util.Long.toFlag(index(x, y)) & b) != 0) {
                    builder.append("[o]");
                } else {
                    builder.append("[ ]");
                }
            }
            if (y != 0) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    public static int index(int x, int y) {
        return x * BUFFERED_HEIGHT + y;
    }
}