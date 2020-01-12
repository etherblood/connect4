package com.etherblood.connect4;

import static com.etherblood.connect4.TokenUtil.FULL_BOARD;
import static com.etherblood.connect4.TokenUtil.generateMoves;
import static com.etherblood.connect4.TokenUtil.isWin;
import static com.etherblood.connect4.TokenUtil.move;
import static com.etherblood.connect4.TokenUtil.occupied;

public class TokenPerft {

    public static void main(String[] args) {
        System.out.println("Warmup...");
        perft(0, 0, 11);

        int depth = 12;
        System.out.println("Computing perft " + depth + "...");
        long start = System.nanoTime();
        long result = perft(0, 0, depth);
        long end = System.nanoTime();
        long durationMillis = (end - start) / 1_000_000;
        String kiloNodesPerSecond = durationMillis == 0 ? "NaN" : Long.toString(result / durationMillis);
        System.out.println("perft result: " + result + " in " + durationMillis + "ms (" + kiloNodesPerSecond + "kn/s)");
    }

    public static long perft(long ownTokens, long opponentTokens, int depth) {
        long free = FULL_BOARD ^ occupied(ownTokens, opponentTokens);
        depth = Math.min(depth, Long.bitCount(free));
        if (depth < 0) {
            throw new IllegalArgumentException();
        }
        if (depth == 0) {
            return 1;
        }
        if (depth == 1) {
            return Long.bitCount(generateMoves(ownTokens, opponentTokens));
        }
        return fastPerft(ownTokens, opponentTokens, depth);
    }

    private static long fastPerft(long ownTokens, long opponentTokens, int depth) {
        assert depth > 1;
        long moves = generateMoves(ownTokens, opponentTokens);
        long sum = 0;
        while (moves != 0) {
            long move = Long.lowestOneBit(moves);
            long nextTokens = move(ownTokens, move);
            if (!isWin(nextTokens)) {
                if(depth == 2) {
                    sum += Long.bitCount(generateMoves(opponentTokens, nextTokens));
                } else {
                    sum += fastPerft(opponentTokens, nextTokens, depth - 1);
                }
            }
            moves ^= move;
        }
        return sum;
    }
}
