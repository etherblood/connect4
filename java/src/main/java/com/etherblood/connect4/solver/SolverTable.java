package com.etherblood.connect4.solver;

import static com.etherblood.connect4.Util.Int.toMask;

public class SolverTable {

    public static final int UNKNOWN_SCORE = 0;
    public static final int WIN_SCORE = 1;
    public static final int DRAW_SCORE = 2;
    public static final int LOSS_SCORE = 3;
    public static final int DRAW_WIN_SCORE = 4;
    public static final int DRAW_LOSS_SCORE = 5;

    private static final int SCORE_BITS = 3;
    private static final int ID_MASK = ~0 << SCORE_BITS;

    private final int[] data;
    private final int mask;
    private long hits, misses, overwrites, stores, loads;

    public SolverTable(int sizeBase) {
        data = new int[1 << sizeBase];
        mask = toMask(sizeBase);
        hits = 0;
        misses = 0;
        overwrites = 0;
        stores = 0;
        loads = 0;
    }

    public int load(long hash) {
        loads++;
        int index = index(hash);
        int rawEntry = data[index];
        if (((rawEntry ^ (int)hash) & ID_MASK) == 0) {
            hits++;
            return rawEntry & toMask(SCORE_BITS);
        }
        misses++;
        return UNKNOWN_SCORE;
    }

    public void store(long hash, int score) {
        stores++;
        int values = 0;
        values |= score & toMask(SCORE_BITS);
        values |= (int)hash & ID_MASK;
        int index = index(hash);
        if (data[index] != 0) {
            overwrites++;
        }
        data[index] = values;
    }

    private int index(long hash) {
        return (int) (hash >>> 32) & mask;
    }

    public void printStats() {
        System.out.println("TT-stats");
        System.out.println(" hits: " + hits);
        System.out.println(" misses: " + misses);
        System.out.println(" overwrites: " + overwrites);
        System.out.println(" loads: " + loads);
        System.out.println(" stores: " + stores);
        int[] scores = new int[6];
        for (int i = 0; i < data.length; i++) {
            scores[data[i] & toMask(SCORE_BITS)]++;
        }
        String[] scoreNames = {"empty", "win", "draw", "loss", "draw+", "draw-"};
        for (int i = 0; i < 6; i++) {
            System.out.println("  " + scoreNames[i] + ": " + scores[i]);
        }
    }
}
