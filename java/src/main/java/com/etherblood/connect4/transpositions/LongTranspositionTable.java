package com.etherblood.connect4.transpositions;

import static com.etherblood.connect4.Util.Int.toMask;
import static com.etherblood.connect4.Util.Long.toFlag;

public class LongTranspositionTable implements TranspositionTable {

    static final int SCORE_BITS = 14;
    static final int DEPTH_BITS = 6;
    static final int TYPE_BITS = 2;
    static final int MOVE_BITS = 6;
    private static final long ID_MASK = ~0L << (SCORE_BITS + DEPTH_BITS + TYPE_BITS + MOVE_BITS);

    private final TableEntry entry = new TableEntry();
    private final long[] data;
    private final int sizeBase, mask;
    private long hits, misses, overwrites, stores, loads;

    public LongTranspositionTable(int sizeBase) {
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
        long hash = 0x9e3779b97f4a7c15L * stateId;
        return (int) (hash >>> (64 - sizeBase)) & mask;
    }

    @Override
    public TableEntry load(long stateId) {
        loads++;
        int index = index(stateId);
        long rawEntry = data[index];
        int values = (int) rawEntry;
        if (((rawEntry ^ stateId) & ID_MASK) == 0) {
            entry.stateId = stateId;
            entry.move = toFlag(values & toMask(MOVE_BITS));
            values >>>= MOVE_BITS;
            entry.score = values & toMask(SCORE_BITS);
            values >>>= SCORE_BITS;
            entry.depth = values & toMask(DEPTH_BITS);
            values >>>= DEPTH_BITS;
            entry.type = values & toMask(TYPE_BITS);
            hits++;
            return entry;
        }
        misses++;
        return null;
    }

    @Override
    public void storeRaw(long stateId, int type, int score, int depth, long move) {
        stores++;
        long values = 0;
        values |= type & toMask(TYPE_BITS);
        values <<= DEPTH_BITS;
        values |= depth & toMask(DEPTH_BITS);
        values <<= SCORE_BITS;
        values |= score & toMask(SCORE_BITS);
        values <<= MOVE_BITS;
        values |= Long.numberOfTrailingZeros(move) & toMask(MOVE_BITS);
        values |= stateId & ID_MASK;
        int index = index(stateId);
        if (data[index] != 0) {
            overwrites++;
        }
        data[index] = values;
    }

    @Override
    public void printStats() {
        System.out.println("TT-stats");
        System.out.println(" hits: " + hits);
        System.out.println(" misses: " + misses);
        System.out.println(" overwrites: " + overwrites);
        System.out.println(" loads: " + loads);
        System.out.println(" stores: " + stores);
    }

}
