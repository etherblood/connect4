package com.etherblood.connect4;

/**
 * Making the fields & methods static improves speed (~25% faster last
 * measured), but it would be much harder to test different board sizes.
 */
public class BoardSettings {

    public final int WIDTH, HEIGHT, BUFFERED_HEIGHT, SIZE;
    public final long FULL_BOARD, LEFT_SIDE, CENTER_BUFFERED_COLUMN;
    public final long EVEN_FILLCOUNT_ROWS, ODD_FILLCOUNT_ROWS;
    public final int RIGHT, RIGHT_DOWN, RIGHT_UP, UP;
    public final boolean IS_HEIGHT_EVEN;

    private final long ROW_0, COLUMN_0, BUFFERED_COLUMN_0;
    private final long[] WIN_CHECK_PATTERNS = new long[4];

    public BoardSettings(int width, int height) {
        WIDTH = width;
        HEIGHT = height;
        SIZE = WIDTH * HEIGHT;

        //computed settings
        BUFFERED_HEIGHT = HEIGHT + 1;
        if (WIDTH * BUFFERED_HEIGHT > Long.SIZE) {
            throw new IllegalArgumentException();
        }
        IS_HEIGHT_EVEN = (HEIGHT & 1) == 0;
        COLUMN_0 = Util.toLongMask(HEIGHT);
        BUFFERED_COLUMN_0 = Util.toLongMask(BUFFERED_HEIGHT);
        ROW_0 = Util.toLongMask(WIDTH * BUFFERED_HEIGHT) / Util.toLongMask(BUFFERED_HEIGHT);
        UP = 1;
        RIGHT = BUFFERED_HEIGHT;
        RIGHT_UP = RIGHT + UP;
        RIGHT_DOWN = RIGHT - UP;
        FULL_BOARD = ROW_0 * COLUMN_0;
        LEFT_SIDE = FULL_BOARD >>> (Util.ceilDiv(WIDTH, 2) * RIGHT);
        CENTER_BUFFERED_COLUMN = (WIDTH & 1) != 0 ? BUFFERED_COLUMN_0 << (WIDTH / 2 * RIGHT) : 0;

        EVEN_FILLCOUNT_ROWS = (COLUMN_0 / 3) * ROW_0;
        ODD_FILLCOUNT_ROWS = FULL_BOARD ^ EVEN_FILLCOUNT_ROWS;

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                WIN_CHECK_PATTERNS[(x + 2 * y) & 3] |= Util.toLongFlag(index(x, y));
            }
        }
    }

    public long mirror(long tokens) {
        if (BUFFERED_HEIGHT == Byte.SIZE) {
            return Long.reverseBytes(tokens) >>> (RIGHT * (Long.BYTES - WIDTH));
        }
        long result = tokens & CENTER_BUFFERED_COLUMN;
        for (int x = 0; x < WIDTH / 2; x++) {
            int mirrorX = WIDTH - x - 1;
            int offset = mirrorX - x;

            result |= (tokens & (BUFFERED_COLUMN_0 << (x * RIGHT))) << (offset * RIGHT);
            result |= (tokens & (BUFFERED_COLUMN_0 << (mirrorX * RIGHT))) >>> (offset * RIGHT);
        }
        return result;
    }

    public static long move(long tokens, long moves) {
        assert (tokens & moves) == 0;
        return tokens | moves;
    }

    public boolean isGameOver(long ownTokens, long opponentTokens) {
        return isBoardFull(ownTokens, opponentTokens) || isWin(opponentTokens);
    }

    public boolean isBoardFull(long ownTokens, long opponentTokens) {
        return occupied(ownTokens, opponentTokens) == FULL_BOARD;
    }

    public boolean isWin(long tokens) {
        return isNonVerticalWin(tokens) || squish(tokens, UP) != 0;
    }

    public boolean isNonVerticalWin(long tokens) {
        return squish(tokens, RIGHT) != 0
                || squish(tokens, RIGHT_DOWN) != 0
                || squish(tokens, RIGHT_UP) != 0;
    }

    public boolean canWin(long ownTokens, long opponentTokens) {
        return (threats(ownTokens, opponentTokens) & generateMoves(ownTokens, opponentTokens)) != 0;
    }

    public long threats(long ownTokens, long opponentTokens) {
        long free = unoccupied(ownTokens, opponentTokens);
        long squishedRight = 0, squishedRightDown = 0, squishedRightUp = 0;
        for (long pattern : WIN_CHECK_PATTERNS) {
            long ownPattern = move(ownTokens, free & pattern);
            squishedRight |= squish(ownPattern, RIGHT);
            squishedRightDown |= squish(ownPattern, RIGHT_DOWN);
            squishedRightUp |= squish(ownPattern, RIGHT_UP);
        }
        long squishedUp = squish(move(ownTokens, generateMoves(ownTokens, opponentTokens)), UP);
        return (stretch(squishedUp, UP)
                | stretch(squishedRight, RIGHT)
                | stretch(squishedRightDown, RIGHT_DOWN)
                | stretch(squishedRightUp, RIGHT_UP))
                & free;
    }

    public static long squish(long tokens, int directionShift) {
        tokens &= tokens >>> (2 * directionShift);
        tokens &= tokens >>> directionShift;
        return tokens;
    }

    public static long stretch(long tokens, int directionShift) {
        tokens |= tokens << (2 * directionShift);
        tokens |= tokens << directionShift;
        return tokens;
    }

    public long generateMoves(long ownTokens, long opponentTokens) {
        return (occupied(ownTokens, opponentTokens) + ROW_0) & FULL_BOARD;
    }

    public static long occupied(long ownTokens, long opponentTokens) {
        return ownTokens | opponentTokens;
    }

    public long unoccupied(long ownTokens, long opponentTokens) {
        return FULL_BOARD ^ occupied(ownTokens, opponentTokens);
    }

    public long upperBoundId() {
        return Util.toLongMask(WIDTH * BUFFERED_HEIGHT);
    }

    public long id(long ownTokens, long opponentTokens) {
        return (occupied(ownTokens, opponentTokens) + ROW_0) ^ ownTokens;
    }

    public String toString(long mask) {
        return toString(mask, 0);
    }

    public String toString(long ownTokens, long opponentTokens) {
        return toString(ownTokens, opponentTokens, 0);
    }

    public String toString(long ownTokens, long opponentTokens, long threats) {
        if ((ownTokens | opponentTokens | FULL_BOARD) != FULL_BOARD) {
            throw new IllegalArgumentException();
        }
        if ((ownTokens & opponentTokens) != 0) {
            throw new IllegalArgumentException();
        }
        StringBuilder builder = new StringBuilder();
        for (int y = HEIGHT - 1; y >= 0; y--) {
            for (int x = 0; x < WIDTH; x++) {
                long flag = Util.toLongFlag(index(x, y));
                if ((threats & flag) != 0) {
                    builder.append('(');
                } else {
                    builder.append('[');
                }
                if ((flag & ownTokens) != 0) {
                    builder.append('x');
                } else if ((flag & opponentTokens) != 0) {
                    builder.append('o');
                } else {
                    builder.append(' ');
                }
                if ((threats & flag) != 0) {
                    builder.append(')');
                } else {
                    builder.append(']');
                }
            }
            if (y != 0) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    public String toString(int[] board, int offset) {
        StringBuilder builder = new StringBuilder();
        for (int y = HEIGHT - 1; y >= 0; y--) {
            for (int x = 0; x < WIDTH; x++) {
                builder.append("[");
                builder.append(board[offset + index(x, y)]);
                builder.append("]");
            }
            if (y != 0) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    public final int index(int x, int y) {
        return x * RIGHT + y;
    }
}
