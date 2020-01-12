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
    private long hits, misses, stores, maxVerifier, maxWork;

    public TwoBig1TranspositionTable(long size) {
        this.table = new long[Math.toIntExact(PrimeUtil.primeLessOrEqual(size / 2))];
        clear();
    }

    @Override
    public int load(long id) {
        long hash = hash(id);
        int verifier = verifier(hash);
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
        maxWork = Math.max(maxWork, work);
        stores++;
        long hash = hash(id);
        int index = index(hash);
        long rawEntry = table[index];
        int previousWork = (int) (rawEntry >>> (2 * (VERIFY_BITS + SCORE_BITS)));
        int verifier = verifier(hash);
        if (Long.compareUnsigned(verifier, maxVerifier) > 0) {
            maxVerifier = verifier;
        }
        long newEntry = (score << VERIFY_BITS) | (verifier & VERIFY_MASK);
        if (work >= previousWork) {
            rawEntry >>>= VERIFY_BITS + SCORE_BITS;
            rawEntry &= Util.toLongMask(VERIFY_BITS + SCORE_BITS);
            rawEntry |= newEntry << (VERIFY_BITS + SCORE_BITS);
            rawEntry |= (long) work << (2 * (VERIFY_BITS + SCORE_BITS));
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
        return (int) Long.remainderUnsigned(hash, table.length);
    }

    private int verifier(long hash) {
        return (int) Long.divideUnsigned(hash, table.length);
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
        System.out.println(" size: " + size + " - " + Util.humanReadableByteCountBin((long) size * Integer.BYTES));
        System.out.println(" hits: " + hits + " - " + Util.toPercentage(hits, hits + misses, 1));
        System.out.println(" misses: " + misses + " - " + Util.toPercentage(misses, hits + misses, 1));
        System.out.println(" overwrites: " + (stores - full));
        System.out.println(" loads: " + (hits + misses));
        System.out.println(" stores: " + stores);
        System.out.println("  " + TranspositionTable.scoreToString(TranspositionTable.UNKNOWN_SCORE) + ": " + scores[TranspositionTable.UNKNOWN_SCORE] + " - " + Util.toPercentage(scores[TranspositionTable.UNKNOWN_SCORE], size, 1));
        System.out.println("  full: " + full + " - " + Util.toPercentage(full, size, 1));
        for (int i = 0; i < 6; i++) {
            if (i == TranspositionTable.UNKNOWN_SCORE) {
                continue;
            }
            System.out.println("   " + TranspositionTable.scoreToString(i) + ": " + scores[i] + " - " + Util.toPercentage(scores[i], full, 1));
        }
        System.out.println(" stores/size: " + 100L * stores / size + "%");
        System.out.println(" used/available key size: " + maxVerifier + "/" + VERIFY_MASK + " - " + Util.toPercentage(maxVerifier, VERIFY_MASK, 1));
        System.out.println(" used/available work size: " + maxWork + "/" + Util.toIntMask(WORK_BITS) + " - " + Util.toPercentage(maxWork, Util.toIntMask(WORK_BITS), 1));
    }

    @Override
    public final void clear() {
        Arrays.fill(table, 0);
        hits = 0;
        misses = 0;
        stores = 0;
        maxVerifier = 0;
    }
}
