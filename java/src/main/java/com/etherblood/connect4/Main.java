package com.etherblood.connect4;

import com.etherblood.connect4.eval.SimpleEvaluation;
import com.etherblood.connect4.transpositions.LongTranspositionTable;

/**
 *
 * @author Philipp
 */
public class Main {

    public static void main(String... args) {
        perft();
//        botGame();
    }

    private static void botGame() {
        Connect4StateImpl state = new Connect4StateImpl();
        SimpleEvaluation eval = new SimpleEvaluation(state);
        AlphaBetaBot bot = new AlphaBetaBot(state, eval, new LongTranspositionTable(22), 15, true, true);
        while(!state.isGameOver()) {
            System.out.println(eval.evaluate());
            long move = bot.search();
            state.move(move);
            System.out.println(state.asString());
            System.out.println();
        }
    }

    private static void perft() {
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
