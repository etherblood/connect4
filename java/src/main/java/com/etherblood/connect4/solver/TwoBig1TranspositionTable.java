package com.etherblood.connect4.solver;

import com.etherblood.connect4.BoardSettings;
import com.etherblood.connect4.Util;
import java.util.Arrays;

public class TwoBig1TranspositionTable implements TranspositionTable {

    private static final int SCORE_BITS = 3;
    private static final int SCORE_MASK = Util.toIntMask(SCORE_BITS);
    private static final int WORK_BITS = 6;
    private static final int VERIFY_BITS = (Long.SIZE - 2 * SCORE_BITS - WORK_BITS) / 2;
    private static final int VERIFY_MASK = Util.toIntMask(VERIFY_BITS);
    private static final int ENTRY_BITS = VERIFY_BITS + SCORE_BITS;

    private final long[] table;
    private long hits, misses, stores;

    public TwoBig1TranspositionTable(long size, BoardSettings board) {
        this.table = new long[Math.toIntExact(PrimeUtil.primeLessOrEqual(size / 2))];
        clear();
        int maxVerifier = verifier(board.upperBoundId());
        if (maxVerifier > VERIFY_MASK) {
            System.out.println("WARNING: Storing of position id's is lossy. Id to index ratio: " + Util.toPercentage(maxVerifier, VERIFY_MASK, 1));
        }
    }

    @Override
    public int load(long id) {
        int verifier = verifier(id);
        int index = index(id);
        long rawEntry = table[index];
        int raw1 = (int) rawEntry;
        if ((raw1 & VERIFY_MASK) == verifier) {
            hits++;
            int score1 = (raw1 >>> VERIFY_BITS) & SCORE_MASK;
            return score1;
        }
        int raw2 = (int) (rawEntry >>> ENTRY_BITS);
        if ((raw2 & VERIFY_MASK) == verifier) {
            hits++;
            int score2 = (raw2 >>> VERIFY_BITS) & SCORE_MASK;
            return score2;
        }
        misses++;
        return UNKNOWN_SCORE;
    }

    @Override
    public void store(long id, long work, int score) {
        assert score == (score & SCORE_MASK);
        assert score != EMPTY_SCORE;
        assert score != UNKNOWN_SCORE;
        assert score != WIN_OR_LOSS_SCORE;
        stores++;
        int workScore = Math.min(Util.toIntMask(WORK_BITS), Util.floorLog(work));
        int index = index(id);
        long rawEntry = table[index];
        int previousWorkScore = (int) (rawEntry >>> (2 * ENTRY_BITS));
        int verifier = verifier(id);
        long newEntry = (score << VERIFY_BITS) | verifier;

        int raw2 = (int) (rawEntry >>> ENTRY_BITS);
        if ((raw2 & VERIFY_MASK) == verifier) {
            rawEntry &= Util.toLongMask(ENTRY_BITS);
            rawEntry |= newEntry << ENTRY_BITS;
            rawEntry |= (long) workScore << (2 * ENTRY_BITS);
        } else {
            if (workScore >= previousWorkScore) {
                rawEntry >>>= ENTRY_BITS;
                rawEntry &= Util.toLongMask(ENTRY_BITS);
                rawEntry |= newEntry << ENTRY_BITS;
                rawEntry |= (long) workScore << (2 * ENTRY_BITS);
            } else {
                rawEntry &= ~Util.toLongMask(ENTRY_BITS);
                rawEntry |= newEntry;
            }
        }

        table[index] = rawEntry;
    }

    private int index(long hash) {
        return (int) Long.remainderUnsigned(hash, table.length);
    }

    private int verifier(long hash) {
        return (int) Long.divideUnsigned(hash, table.length) & VERIFY_MASK;
    }

    @Override
    public void printStats() {
        int[] scores = new int[8];
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
        System.out.println("  unused: " + scores[TranspositionTable.UNKNOWN_SCORE] + " - " + Util.toPercentage(scores[TranspositionTable.UNKNOWN_SCORE], size, 1));
        System.out.println("  full: " + full + " - " + Util.toPercentage(full, size, 1));
        for (int i = 0; i < scores.length; i++) {
            if (i == TranspositionTable.UNKNOWN_SCORE || i == TranspositionTable.EMPTY_SCORE || i == TranspositionTable.WIN_OR_LOSS_SCORE) {
                continue;
            }
            System.out.println("   " + TranspositionTable.scoreToString(i) + ": " + scores[i] + " - " + Util.toPercentage(scores[i], full, 1));
        }
        System.out.println(" stores/size: " + 100L * stores / size + "%");
    }

    @Override
    public final void clear() {
        Arrays.fill(table, ~0);
        hits = 0;
        misses = 0;
        stores = 0;
    }
}
