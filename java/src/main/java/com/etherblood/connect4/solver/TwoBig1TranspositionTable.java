package com.etherblood.connect4.solver;

import com.etherblood.connect4.BoardSettings;
import com.etherblood.connect4.Util;
import java.util.Arrays;
import java.util.List;

public class TwoBig1TranspositionTable implements TranspositionTable {

    private static final int SCORE_BITS = 3;
    private static final int SCORE_MASK = Util.toIntMask(SCORE_BITS);
    private static final int WORK_BITS = 6;
    private static final int WORK_MASK = Util.toIntMask(WORK_BITS);
    private static final int VERIFY_BITS = (Long.SIZE - 2 * SCORE_BITS - WORK_BITS) / 2;
    private static final int VERIFY_MASK = Util.toIntMask(VERIFY_BITS);
    private static final int ENTRY_BITS = VERIFY_BITS + SCORE_BITS;
    private static final long ENTRY_MASK = Util.toLongMask(ENTRY_BITS);

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
        assert score != UNKNOWN_SCORE;
        stores++;
        long workScore = Math.min(WORK_MASK, Util.floorLog(work));
        int index = index(id);
        long rawEntry = table[index];
        int verifier = verifier(id);
        long newEntry = (score << VERIFY_BITS) | verifier;
        assert (newEntry & ENTRY_MASK) == newEntry;

        int raw2 = (int) (rawEntry >>> ENTRY_BITS);
        if ((raw2 & VERIFY_MASK) == verifier) {
            rawEntry &= ENTRY_MASK;
            rawEntry |= newEntry << ENTRY_BITS;
            rawEntry |= workScore << (2 * ENTRY_BITS);
        } else {
            long previousWorkScore = rawEntry >>> (2 * ENTRY_BITS);
            if (workScore >= previousWorkScore) {
                rawEntry >>>= ENTRY_BITS;
                rawEntry &= ENTRY_MASK;
                rawEntry |= newEntry << ENTRY_BITS;
                rawEntry |= workScore << (2 * ENTRY_BITS);
            } else {
                rawEntry &= ~ENTRY_MASK;
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
        long[] scores = new long[8];
        for (int i = 0; i < table.length; i++) {
            long raw = table[i];
            scores[(int) ((raw >>> VERIFY_BITS) & SCORE_MASK)]++;
            scores[(int) ((raw >>> (VERIFY_BITS + ENTRY_BITS)) & SCORE_MASK)]++;
        }

        long size = 2 * table.length;
        long full = size - scores[TranspositionTable.UNKNOWN_SCORE];
        System.out.println(" size: " + size + " - " + Util.humanReadableByteCountBin(size * Integer.BYTES));
        System.out.println(" hits: " + hits + " - " + Util.toPercentage(hits, hits + misses, 1));
        System.out.println(" misses: " + misses + " - " + Util.toPercentage(misses, hits + misses, 1));
        System.out.println(" overwrites: " + (stores - full));
        System.out.println(" loads: " + (hits + misses));
        System.out.println(" stores: " + stores);
        System.out.println("  unused: " + scores[TranspositionTable.UNKNOWN_SCORE] + " - " + Util.toPercentage(scores[TranspositionTable.UNKNOWN_SCORE], size, 1));
        System.out.println("  full: " + full + " - " + Util.toPercentage(full, size, 1));
        List<Integer> usefulScores = Arrays.asList(WIN_SCORE, DRAW_SCORE, LOSS_SCORE, DRAW_OR_WIN_SCORE, DRAW_OR_LOSS_SCORE);
        for (int usefulScore : usefulScores) {
            System.out.println("   " + TranspositionTable.scoreToString(usefulScore) + ": " + scores[usefulScore] + " - " + Util.toPercentage(scores[usefulScore], full, 1));
        }
        System.out.println(" stores/size: " + 100L * stores / size + "%");
    }

    @Override
    public final void clear() {
        long unknownScores = ((long) UNKNOWN_SCORE << VERIFY_BITS) | ((long) UNKNOWN_SCORE << (VERIFY_BITS + ENTRY_BITS));
        Arrays.fill(table, unknownScores);
        hits = 0;
        misses = 0;
        stores = 0;
    }
}
