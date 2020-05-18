package com.etherblood.connect4.sandbox;

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
        System.out.println("perft result: " + result + " in " + durationMillis + "ms");
    }

    public static long perft(long ownTokens, long opponentTokens, int depth) {
        long free = TokenUtil.FULL_BOARD ^ TokenUtil.occupied(ownTokens, opponentTokens);
        depth = Math.min(depth, Long.bitCount(free));
        if (depth < 0) {
            throw new IllegalArgumentException();
        }
        if (depth == 0) {
            return 1;
        }
        if (depth == 1) {
            return Long.bitCount(TokenUtil.generateMoves(ownTokens, opponentTokens));
        }
        return fastPerft(ownTokens, opponentTokens, depth);
    }

    private static long fastPerft(long ownTokens, long opponentTokens, int depth) {
        assert depth > 1;
        long moves = TokenUtil.generateMoves(ownTokens, opponentTokens);
        long sum = 0;
        while (moves != 0) {
            long move = Long.lowestOneBit(moves);
            long nextTokens = TokenUtil.move(ownTokens, move);
            if (!TokenUtil.isWin(nextTokens)) {
                if(depth == 2) {
                    sum += Long.bitCount(TokenUtil.generateMoves(opponentTokens, nextTokens));
                } else {
                    sum += fastPerft(opponentTokens, nextTokens, depth - 1);
                }
            }
            moves ^= move;
        }
        return sum;
    }
}
