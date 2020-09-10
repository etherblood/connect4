package com.etherblood.connect4.solver;

import com.etherblood.connect4.Util;
import static com.etherblood.connect4.solver.TranspositionTable.DRAW_OR_LOSS_SCORE;
import static com.etherblood.connect4.solver.TranspositionTable.DRAW_OR_WIN_SCORE;
import static com.etherblood.connect4.solver.TranspositionTable.DRAW_SCORE;
import static com.etherblood.connect4.solver.TranspositionTable.LOSS_SCORE;
import static com.etherblood.connect4.solver.TranspositionTable.WIN_SCORE;
import java.util.Arrays;
import java.util.List;

public class TranspositionTableImpl implements TranspositionTable {

    private static final int SCORE_BITS = 3;
    private static final int VERIFY_BITS = Integer.SIZE - SCORE_BITS;
    private static final int VERIFY_MASK = Util.toIntMask(VERIFY_BITS);

    private final int[] table;
    private long hits, misses, stores;

    public TranspositionTableImpl(long size) {
        long prime = PrimeUtil.primeLessOrEqual(size);
        if (size != prime) {
            System.out.println("WARN: Automatically adjusted table size from " + size + " to " + prime + " for improved indexing.");
        }
        this.table = new int[Math.toIntExact(prime)];
        clear();
    }

    @Override
    public int load(long id) {
        long hash = hash(id);
        int index = index(hash);
        int rawEntry = table[index];
        if (((rawEntry ^ (int) (verifier(hash))) & VERIFY_MASK) == 0) {
            hits++;
            return rawEntry >>> VERIFY_BITS;
        }
        misses++;
        return UNKNOWN_SCORE;
    }

    @Override
    public void store(long id, long work, int score) {
        assert (score >>> SCORE_BITS) == 0;
        assert score != UNKNOWN_SCORE;
        stores++;
        long hash = hash(id);
        int index = index(hash);
        table[index] = (score << VERIFY_BITS) | ((int) verifier(hash) & VERIFY_MASK);
    }

    private long hash(long id) {
        return id;
    }

    private int index(long hash) {
        return (int) Long.remainderUnsigned(hash, table.length);
    }

    private long verifier(long hash) {
        return Long.divideUnsigned(hash, table.length);
    }

    @Override
    public void printStats() {
        int[] scores = new int[8];
        for (int i = 0; i < table.length; i++) {
            scores[table[i] >>> VERIFY_BITS]++;
        }
        int full = table.length - scores[TranspositionTable.UNKNOWN_SCORE];
        System.out.println(" size: " + table.length);
        System.out.println(" hits: " + hits);
        System.out.println(" misses: " + misses);
        System.out.println(" overwrites: " + (stores - full));
        System.out.println(" loads: " + (hits + misses));
        System.out.println(" stores: " + stores);
        System.out.println("  unused: " + scores[TranspositionTable.UNKNOWN_SCORE]);
        System.out.println("  full: " + (table.length - scores[TranspositionTable.UNKNOWN_SCORE]));
        List<Integer> usefulScores = Arrays.asList(WIN_SCORE, DRAW_SCORE, LOSS_SCORE, DRAW_OR_WIN_SCORE, DRAW_OR_LOSS_SCORE);
        for (int usefulScore : usefulScores) {
            System.out.println("   " + TranspositionTable.scoreToString(usefulScore) + ": " + scores[usefulScore]);
        }
    }

    @Override
    public final void clear() {
        int unknownScore = UNKNOWN_SCORE << VERIFY_BITS;
        Arrays.fill(table, unknownScore);
        hits = 0;
        misses = 0;
        stores = 0;
    }
}
