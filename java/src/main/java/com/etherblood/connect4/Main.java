package com.etherblood.connect4;

/**
 *
 * @author Philipp
 */
public class Main {

    public static void main(String... args) {
        Connect4StateImpl state = new Connect4StateImpl();
        System.out.println("Warmup...");
        new Perft(state).perft(11);
        System.out.println();

        int depth = 12;
        perft(state, depth);
    }

    private static void perft(Connect4State state, int depth) {
        System.out.println("Computing perft " + depth + "...");
        long start = System.nanoTime();
        Perft perft = new Perft(state);
        long result = perft.perft(depth);
        long end = System.nanoTime();
        long durationMillis = (end - start) / 1_000_000;
        String kiloNodesPerSecond = durationMillis == 0 ? "NaN" : Long.toString(result / durationMillis);
        System.out.println("perft: " + result + " in " + durationMillis + "ms (" + kiloNodesPerSecond + "kn/s)");
    }
}
