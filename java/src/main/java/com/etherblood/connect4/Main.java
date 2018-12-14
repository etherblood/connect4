package com.etherblood.connect4;

/**
 *
 * @author Philipp
 */
public class Main {

    public static void main(String... args) {
        System.out.println("Warmup...");
        new Perft(new FastConnect4State()).perft(11);
        new Perft(new SimpleConnect4State()).perft(11);
        
        int depth = 12;
        System.out.println();
        System.out.println("Fast:");
        perft(new FastConnect4State(), depth);
        System.out.println();
        System.out.println("Simple:");
        perft(new SimpleConnect4State(), depth);
    }
    
    private static void perft(Connect4State state, int depth) {
        System.out.println("Computing perft " + depth + "...");
        long start = System.nanoTime();
        long result = new Perft(state).perft(depth);
        long end = System.nanoTime();
        long durationMillis = (end - start) / 1_000_000;
        System.out.println("perft: " + result + " in " + durationMillis + "ms (" + result / durationMillis + "kn/s)");
    }
}
