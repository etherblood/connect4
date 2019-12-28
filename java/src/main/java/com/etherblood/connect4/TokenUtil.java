package com.etherblood.connect4;

import static com.etherblood.connect4.Util.Long.toMask;

public class TokenUtil {

    private static final long GOLDEN_MULTIPLIER = 0x9e3779b97f4a7c15L;
    public static final int WIDTH, HEIGHT, BUFFERED_HEIGHT;
    public static final long ROW_0, COLUMN_0, BUFFERED_COLUMN_0;
    public static final long FULL_BOARD, LEFT_SIDE, CENTER_COLUMN;
    public static final long EVEN_INDEX_ROWS, ODD_INDEX_ROWS;
    public static final int RIGHT, RIGHT_DOWN, RIGHT_UP, UP;
    public static final boolean IS_HEIGHT_EVEN;

    public static final long[] WIN_CHECK_PATTERNS;

    static {
        WIDTH = 7;
        HEIGHT = 6;

        BUFFERED_HEIGHT = HEIGHT + 1;
        if (WIDTH * BUFFERED_HEIGHT > Long.SIZE) {
            throw new IllegalArgumentException();
        }
        IS_HEIGHT_EVEN = (HEIGHT & 1) == 0;
        COLUMN_0 = toMask(HEIGHT);
        BUFFERED_COLUMN_0 = toMask(BUFFERED_HEIGHT);
        ROW_0 = toMask(WIDTH * BUFFERED_HEIGHT) / toMask(BUFFERED_HEIGHT);
        UP = 1;
        RIGHT = BUFFERED_HEIGHT;
        RIGHT_UP = RIGHT + UP;
        RIGHT_DOWN = RIGHT - UP;
        FULL_BOARD = ROW_0 * COLUMN_0;
        LEFT_SIDE = FULL_BOARD >>> (Util.Int.ceilDiv(WIDTH, 2) * RIGHT);
        CENTER_COLUMN = (WIDTH & 1) != 0 ? COLUMN_0 << (WIDTH / 2 * RIGHT) : 0;

        EVEN_INDEX_ROWS = (COLUMN_0 / 3) * ROW_0;
        ODD_INDEX_ROWS = (BUFFERED_COLUMN_0 / 3) * ROW_0;

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
            if (((left ^ right) & COLUMN_0) != 0) {
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

            result |= (tokens & (COLUMN_0 << (x * RIGHT))) << (offset * RIGHT);
            result |= (tokens & (COLUMN_0 << (mirrorX * RIGHT))) >>> (offset * RIGHT);
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

    public static boolean isNonVerticalWin(long tokens) {
        return squish(tokens, RIGHT) != 0
                || squish(tokens, RIGHT_DOWN) != 0
                || squish(tokens, RIGHT_UP) != 0;
    }

    public static boolean canWin(long tokens, long moves) {
        for (long pattern : WIN_CHECK_PATTERNS) {
            long patternMoves = moves & pattern;
            if (patternMoves != 0 && isWin(move(tokens, patternMoves))) {
                return true;
            }
        }
        return false;
    }

    public static long findAnyWinningMove(long tokens, long moves) {
        long squished = squish(move(tokens, moves), UP);
        if (squished != 0) {
            return Long.lowestOneBit(stretch(squished, UP) & moves);
        }
        for (long pattern : WIN_CHECK_PATTERNS) {
            long patternMoves = moves & pattern;
            if (patternMoves != 0) {
                long moved = move(tokens, patternMoves);
                squished = squish(moved, RIGHT);
                if (squished != 0) {
                    return Long.lowestOneBit(stretch(squished, RIGHT) & moves);
                }

                squished = squish(moved, RIGHT_DOWN);
                if (squished != 0) {
                    return Long.lowestOneBit(stretch(squished, RIGHT_DOWN) & moves);
                }

                squished = squish(moved, RIGHT_UP);
                if (squished != 0) {
                    return Long.lowestOneBit(stretch(squished, RIGHT_UP) & moves);
                }
            }
        }
        return 0;
    }

    public static long threats(long ownTokens, long opponentTokens) {
        long free = unoccupied(ownTokens, opponentTokens);
        long squishedRight = 0, squishedRightDown = 0, squishedRightUp = 0;
        for (long pattern : TokenUtil.WIN_CHECK_PATTERNS) {
            long ownPattern = TokenUtil.move(ownTokens, free & pattern);
            squishedRight |= TokenUtil.squish(ownPattern, TokenUtil.RIGHT);
            squishedRightDown |= TokenUtil.squish(ownPattern, TokenUtil.RIGHT_DOWN);
            squishedRightUp |= TokenUtil.squish(ownPattern, TokenUtil.RIGHT_UP);
        }
        long squishedUp = TokenUtil.squish(TokenUtil.move(ownTokens, generateMoves(ownTokens, opponentTokens)), TokenUtil.UP);
        return (TokenUtil.stretch(squishedUp, TokenUtil.UP)
                | TokenUtil.stretch(squishedRight, TokenUtil.RIGHT)
                | TokenUtil.stretch(squishedRightDown, TokenUtil.RIGHT_DOWN)
                | TokenUtil.stretch(squishedRightUp, TokenUtil.RIGHT_UP))
                & free;
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
        return (occupied(ownTokens, opponentTokens) + ROW_0) & FULL_BOARD;
    }

    public static long occupied(long ownTokens, long opponentTokens) {
        return ownTokens | opponentTokens;
    }

    public static long unoccupied(long ownTokens, long opponentTokens) {
        return FULL_BOARD ^ occupied(ownTokens, opponentTokens);
    }

    public static long hash(long ownTokens, long opponentTokens) {
        return GOLDEN_MULTIPLIER * id(ownTokens, opponentTokens);
    }

    public static long id(long ownTokens, long opponentTokens) {
        return (occupied(ownTokens, opponentTokens) + ROW_0) ^ ownTokens;
    }

    public static String toString(long mask) {
        return toString(mask, 0);
    }

    public static String toString(long ownTokens, long opponentTokens) {
        if ((ownTokens | opponentTokens | FULL_BOARD) != FULL_BOARD) {
            throw new IllegalArgumentException();
        }
        if ((ownTokens & opponentTokens) != 0) {
            throw new IllegalArgumentException();
        }
        StringBuilder builder = new StringBuilder();
        for (int y = HEIGHT - 1; y >= 0; y--) {
            for (int x = 0; x < WIDTH; x++) {
                if ((Util.Long.toFlag(index(x, y)) & ownTokens) != 0) {
                    builder.append("[x]");
                } else if ((Util.Long.toFlag(index(x, y)) & opponentTokens) != 0) {
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
