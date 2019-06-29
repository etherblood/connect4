package com.etherblood.connect4.solver;

import static com.etherblood.connect4.Util.Int.toMask;

public class SolverTable {

    private static final long GOLDEN_MULTIPLIER = 0x9e3779b97f4a7c15L;
    public static final int UNKNOWN_SCORE = 0;
    public static final int WIN_SCORE = 1;
    public static final int DRAW_SCORE = 2;
    public static final int LOSS_SCORE = 3;
    public static final int DRAW_WIN_SCORE = 4;
    public static final int DRAW_LOSS_SCORE = 5;

    private static final int SCORE_BITS = 3;
    private static final long ID_MASK = ~0L << SCORE_BITS;

    private final long[] data;
    private final int sizeBase, mask;
    private long hits, misses, overwrites, stores, loads;

    public SolverTable(int sizeBase) {
        this.sizeBase = sizeBase;
        data = new long[1 << sizeBase];
        mask = toMask(sizeBase);
        hits = 0;
        misses = 0;
        overwrites = 0;
        stores = 0;
        loads = 0;
    }

    private int index(long stateId) {
        long hash = GOLDEN_MULTIPLIER * stateId;
        return (int) (hash >>> (Long.SIZE - sizeBase)) & mask;
    }

    public int load(long stateId) {
        loads++;
        int index = index(stateId);
        long rawEntry = data[index];
        if (((rawEntry ^ stateId) & ID_MASK) == 0) {
            hits++;
            return (int) rawEntry & toMask(SCORE_BITS);
        }
        misses++;
        return UNKNOWN_SCORE;
    }

    public void store(long stateId, int score) {
        stores++;
        long values = 0;
        values |= score & toMask(SCORE_BITS);
        values |= stateId & ID_MASK;
        int index = index(stateId);
        if (data[index] != 0) {
            overwrites++;
        }
        data[index] = values;
    }

    public void printStats() {
        System.out.println("TT-stats");
        System.out.println(" hits: " + hits);
        System.out.println(" misses: " + misses);
        System.out.println(" overwrites: " + overwrites);
        System.out.println(" loads: " + loads);
        System.out.println(" stores: " + stores);
        int full = 0, empty = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0) {
                empty++;
            } else {
                full++;
            }
        }
        System.out.println(" full: " + full);
        System.out.println(" empty: " + empty);
    }
}
