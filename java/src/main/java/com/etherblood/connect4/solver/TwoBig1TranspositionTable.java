package com.etherblood.connect4.solver;

import com.etherblood.connect4.Util;
import java.util.Arrays;

public class TwoBig1TranspositionTable implements TranspositionTable {

    private static final int SCORE_BITS = 3;
    private static final int SCORE_MASK = Util.toIntMask(SCORE_BITS);
    private static final int WORK_BITS = 6;
    private static final int VERIFY_BITS = (Long.SIZE - 2 * SCORE_BITS - WORK_BITS) / 2;
    private static final int VERIFY_MASK = Util.toIntMask(VERIFY_BITS);

    private final long[] table;
    private long hits, misses, stores;

    public TwoBig1TranspositionTable(long size) {
        long prime = PrimeUtil.primeLessOrEqual(size);
        if (size != prime) {
            System.out.println("Automatically adjusted table size from " + size + " to " + prime + " for improved indexing.");
        }
        this.table = new long[Math.toIntExact(prime)];
        clear();
    }

    @Override
    public int load(long id) {
        long hash = hash(id);
        int verifier = (int) (verifier(hash));
        int index = index(hash);
        long rawEntry = table[index];
        int raw1 = (int) (rawEntry);
        int raw2 = (int) (rawEntry >>> (VERIFY_BITS + SCORE_BITS));
        int score1 = (int) (raw1 >>> VERIFY_BITS) & SCORE_MASK;
        int score2 = (int) (raw2 >>> VERIFY_BITS) & SCORE_MASK;
        if (((raw1 ^ verifier) & VERIFY_MASK) == 0) {
            hits++;
            return score1;
        }
        if (((raw2 ^ verifier) & VERIFY_MASK) == 0) {
            hits++;
            return score2;
        }
        misses++;
        return UNKNOWN_SCORE;
    }

    @Override
    public void store(long id, int work, int score) {
        assert (score >>> SCORE_BITS) == 0;
        assert (work >>> WORK_BITS) == 0;
        stores++;
        long hash = hash(id);
        int index = index(hash);
        long rawEntry = table[index];
        int previousWork = (int) (rawEntry >>> (2 * (VERIFY_BITS + SCORE_BITS)));
        long newEntry = (score << VERIFY_BITS) | (verifier(hash) & VERIFY_MASK);
        if (work >= previousWork) {
            rawEntry >>>= VERIFY_BITS + SCORE_BITS;
            rawEntry &= Util.toLongMask(VERIFY_BITS + SCORE_BITS);
            rawEntry |= newEntry << (VERIFY_BITS + SCORE_BITS);
            rawEntry |= (long)work << (2 * (VERIFY_BITS + SCORE_BITS));
        } else {
            rawEntry &= ~Util.toLongMask(VERIFY_BITS + SCORE_BITS);
            rawEntry |= newEntry;
        }
        table[index] = rawEntry;
    }

    private long hash(long id) {
        return id;
    }

    private int index(long hash) {
        return Math.floorMod(hash, table.length);
    }

    private long verifier(long hash) {
        return Math.floorDiv(hash, table.length);
    }

    @Override
    public void printStats() {
        int[] scores = new int[6];
        for (int i = 0; i < table.length; i++) {
            long raw = table[i];
            scores[(int) ((raw >>> VERIFY_BITS) & SCORE_MASK)]++;
            scores[(int) ((raw >>> (2 * VERIFY_BITS + SCORE_BITS)) & SCORE_MASK)]++;
        }

        int size = 2 * table.length;
        int full = size - scores[TranspositionTable.UNKNOWN_SCORE];
        System.out.println(" size: " + size);
        System.out.println(" hits: " + hits);
        System.out.println(" misses: " + misses);
        System.out.println(" overwrites: " + (stores - full));
        System.out.println(" loads: " + (hits + misses));
        System.out.println(" stores: " + stores);
        System.out.println("  " + TranspositionTable.scoreToString(TranspositionTable.UNKNOWN_SCORE) + ": " + scores[TranspositionTable.UNKNOWN_SCORE]);
        System.out.println("  full: " + (size - scores[TranspositionTable.UNKNOWN_SCORE]));
        for (int i = 0; i < 6; i++) {
            if (i == TranspositionTable.UNKNOWN_SCORE) {
                continue;
            }
            System.out.println("   " + TranspositionTable.scoreToString(i) + ": " + scores[i]);
        }
    }

    @Override
    public final void clear() {
        Arrays.fill(table, 0);
        hits = 0;
        misses = 0;
        stores = 0;
    }
}
