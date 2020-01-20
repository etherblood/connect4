package com.etherblood.connect4;

import java.util.Arrays;

public class MaskPerft {

    private static final long CARRY_MASK;
    private static final long[] MOVE_MASKS;

    static {
        CARRY_MASK = ~Long.divideUnsigned(-1, 3);
        MOVE_MASKS = new long[3 * TokenUtil.WIDTH * TokenUtil.BUFFERED_HEIGHT];

        long iterator = TokenUtil.FULL_BOARD;
        while (iterator != 0) {
            int moveIndex = Long.numberOfTrailingZeros(iterator);
            long move = 1L << moveIndex;
            iterator ^= move;
            MOVE_MASKS[3 * moveIndex] |= move;
        }

        int group = -Math.floorDiv(TokenUtil.WIDTH * TokenUtil.BUFFERED_HEIGHT - 1, -2);
        for (int direction : Arrays.asList(TokenUtil.RIGHT, TokenUtil.UP, TokenUtil.RIGHT_DOWN, TokenUtil.RIGHT_UP)) {
            long fullSquished = TokenUtil.squish(TokenUtil.FULL_BOARD, direction);
            while (fullSquished != 0) {
                long squished = Long.lowestOneBit(fullSquished);
                fullSquished ^= squished;
                long items = TokenUtil.stretch(squished, direction);
                while (items != 0) {
                    int itemIndex = Long.numberOfTrailingZeros(items);
                    long item = 1L << itemIndex;
                    items ^= item;

                    MOVE_MASKS[3 * itemIndex + group / 32] |= 1L << (2 * (group % 32));
                }
                group++;
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Warmup...");
        perft(11);

        int depth = 12;
        System.out.println("Computing perft " + depth + "...");
        long start = System.nanoTime();
        long result = perft(depth);
        long end = System.nanoTime();
        long durationMillis = (end - start) / 1_000_000;
        System.out.println("perft result: " + result + " in " + durationMillis + "ms");
    }

    public static long perft(int depth) {
        long free = TokenUtil.unoccupied(0, 0);
        depth = Math.min(depth, Long.bitCount(free));
        if (depth < 0) {
            throw new IllegalArgumentException();
        }
        if (depth == 0) {
            return 1;
        }
        if (depth == 1) {
            return Long.bitCount(TokenUtil.generateMoves(0, 0));
        }
        return maskPerft(0, 0, 0, 0, 0, 0, depth);
    }

    private static long maskPerft(long own0, long own1, long own2, long opp0, long opp1, long opp2, int depth) {
        long moves = (own0 + opp0 + TokenUtil.ROW_0) & TokenUtil.FULL_BOARD;
        long sum = 0;
        while (moves != 0) {
            int moveIndex = Long.numberOfTrailingZeros(moves);
            long move0 = MOVE_MASKS[3 * moveIndex];
            moves &= ~move0;
            long new0 = own0 + move0;
            if (((own0 & ~new0) & CARRY_MASK) != 0) {
                continue;
            }
            long new1 = own1 + MOVE_MASKS[3 * moveIndex + 1];
            if (((own1 & ~new1) & CARRY_MASK) != 0) {
                continue;
            }
            long new2 = own2 + MOVE_MASKS[3 * moveIndex + 2];
            if (((own2 & ~new2) & CARRY_MASK) != 0) {
                continue;
            }
            if (depth == 2) {
                sum += Long.bitCount((new0 + opp0 + TokenUtil.ROW_0) & TokenUtil.FULL_BOARD);
            } else {
                sum += maskPerft(opp0, opp1, opp2, new0, new1, new2, depth - 1);
            }
        }
        return sum;
    }
}
