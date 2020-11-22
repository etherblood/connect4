package com.etherblood.connect4.sandbox;

public class TicTacToeMaskPerft {

    private static final int FULL_BOARD;

    private static final int EMPTY_WINS;
    private static final int WIN_MASK;
    private static final int[] MOVE_WINS;

    static {
        int empty = 0b001_001_001_001_001_001_001_001;
        int move0 = 0b000_001_000_000_001_000_000_001;
        int move1 = 0b000_000_000_001_000_000_000_001;
        int move2 = 0b001_000_001_000_000_000_000_001;
        int move3 = 0b000_000_000_000_001_000_001_000;
        int move4 = 0b001_001_000_001_000_000_001_000;
        int move5 = 0b000_000_001_000_000_000_001_000;
        int move6 = 0b001_000_000_000_001_001_000_000;
        int move7 = 0b000_000_000_001_000_001_000_000;
        int move8 = 0b000_001_001_000_000_001_000_000;
        int _wins = 0b100_100_100_100_100_100_100_100;

        EMPTY_WINS = empty;
        MOVE_WINS = new int[]{move0, move1, move2, move3, move4, move5, move6, move7, move8};
        WIN_MASK = _wins;

        FULL_BOARD = 0b111_111_111;
    }

    public static void main(String[] args) {
        //warmup
        for (int i = 0; i < 1000; i++) {
            iterativePerft();
        }

        int result = -1;
        int iterations = 100;
        long startNanos = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            result = iterativePerft();
        }
        long nanos = System.nanoTime() - startNanos;
        System.out.println("perft: " + result);
        System.out.println("nanos: " + nanos / iterations);
    }

    private static int iterativePerft() {
        int count = 0;
        int[] stack = new int[3 * 34];
        int pointer = 0;
        stack[pointer++] = EMPTY_WINS;
        stack[pointer++] = EMPTY_WINS;
        stack[pointer++] = FULL_BOARD;

        while (pointer != 0) {
            int moves = stack[--pointer];
            int ownWins = stack[--pointer];
            int oppWins = stack[--pointer];
            int iterator = moves;
            while (iterator != 0) {
                int move = Integer.lowestOneBit(iterator);
                int nextMoves = moves ^ move;
                int moveIndex = Long.numberOfTrailingZeros(iterator);
                int nextWins = ownWins + MOVE_WINS[moveIndex];
                if ((nextWins & WIN_MASK) == 0) {
                    if (Integer.bitCount(nextMoves) == 1) {
                        count++;
                    } else {
                        stack[pointer++] = nextWins;
                        stack[pointer++] = oppWins;
                        stack[pointer++] = nextMoves;
                    }
//                } else {
//                    // with this we would count number of games instead of perft
//                    count++;
                }
                iterator &= iterator - 1;
            }
        }
        return count;
    }

    private static int perft() {
        return perft(FULL_BOARD, EMPTY_WINS, EMPTY_WINS);
    }

    private static int perft(int moves, int ownWins, int oppWins) {
        int count = 0;
        int iterator = moves;
        while (iterator != 0) {
            int move = Integer.lowestOneBit(iterator);
            int nextMoves = moves ^ move;
            if (nextMoves == 0) {
                count++;
            } else {
                int moveIndex = Long.numberOfTrailingZeros(iterator);
                int nextWins = ownWins + MOVE_WINS[moveIndex];
                if ((nextWins & WIN_MASK) == 0) {
                    count += perft(nextMoves, oppWins, nextWins);
//                } else {
//                    // with this we would count number of games instead of perft
//                    count++;
                }
            }
            iterator &= iterator - 1;
        }
        return count;
    }
}
