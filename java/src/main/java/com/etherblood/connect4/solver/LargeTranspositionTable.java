package com.etherblood.connect4.solver;

import static com.etherblood.connect4.Util.Int.toMask;

public class LargeTranspositionTable implements TranspositionTable {

    private static final int SCORE_BITS = 3;
    private static final int SCORE_MASK = toMask(SCORE_BITS);
    private static final int ID_MASK = ~SCORE_MASK;

    private final int[] data;
    private final int indexMask;
    private long hits, misses, overwrites, stores, loads;

    public LargeTranspositionTable(int sizeBase) {
        data = new int[1 << sizeBase];
        indexMask = toMask(sizeBase);
        hits = 0;
        misses = 0;
        overwrites = 0;
        stores = 0;
        loads = 0;
    }

    @Override
    public int load(long hash) {
        loads++;
        int index = index(hash);
        int rawEntry = data[index];
        if (((rawEntry ^ (int)hash) & ID_MASK) == 0) {
            hits++;
            return rawEntry & SCORE_MASK;
        }
        misses++;
        return UNKNOWN_SCORE;
    }

    @Override
    public void store(long hash, int score) {
        stores++;
        int index = index(hash);
        if (data[index] != 0) {
            overwrites++;
        }
        data[index] = (score & SCORE_MASK) | ((int)hash & ID_MASK);
    }

    private int index(long hash) {
        return (int) (hash >>> 32) & indexMask;
    }

    @Override
    public void printStats() {
        System.out.println("TT-stats");
        System.out.println(" size: " + data.length);
        System.out.println(" hits: " + hits);
        System.out.println(" misses: " + misses);
        System.out.println(" overwrites: " + overwrites);
        System.out.println(" loads: " + loads);
        System.out.println(" stores: " + stores);
        int[] scores = new int[6];
        for (int i = 0; i < data.length; i++) {
            scores[data[i] & SCORE_MASK]++;
        }
        String[] scoreNames = {"empty", "win", "draw", "loss", "draw+", "draw-"};
        for (int i = 0; i < 6; i++) {
            System.out.println("  " + scoreNames[i] + ": " + scores[i]);
        }
    }
}
