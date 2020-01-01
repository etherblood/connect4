package com.etherblood.connect4.solver;

import com.etherblood.connect4.Util;
import java.util.Arrays;

public class TranspositionTableImpl implements TranspositionTable {

    private static final int SCORE_BITS = 3;
    private static final int VERIFY_BITS = Integer.SIZE - SCORE_BITS;
    private static final int VERIFY_MASK = Util.toIntMask(VERIFY_BITS);

    private final int[] table;
    private long hits, misses, overwrites, stores;

    public TranspositionTableImpl(long size) {
        long prime = PrimeUtil.primeLessOrEqual(size);
        if (size != prime) {
            System.out.println("WARN: Automatically adjusted table size from " + size + " to " + prime + " for improved indexing.");
        }
        this.table = new int[(int) prime];
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
    public void store(long id, int score) {
        assert (score >>> SCORE_BITS) == 0;
        stores++;
        long hash = hash(id);
        int index = index(hash);
        if (table[index] != 0) {
            overwrites++;
        }
        table[index] = (score << VERIFY_BITS) | ((int) verifier(hash) & VERIFY_MASK);
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
        System.out.println(" size: " + table.length);
        System.out.println(" hits: " + hits);
        System.out.println(" misses: " + misses);
        System.out.println(" overwrites: " + overwrites);
        System.out.println(" loads: " + (hits + misses));
        System.out.println(" stores: " + stores);
        int[] scores = new int[6];
        for (int i = 0; i < table.length; i++) {
            scores[table[i] >>> VERIFY_BITS]++;
        }
        String[] scoreNames = {"empty", "win", "draw", "loss", "draw+", "draw-"};
        System.out.println("  " + scoreNames[0] + ": " + scores[0]);
        System.out.println("  full: " + (table.length - scores[0]));
        for (int i = 1; i < 6; i++) {
            System.out.println("   " + scoreNames[i] + ": " + scores[i]);
        }
    }

    @Override
    public final void clear() {
        Arrays.fill(table, 0);
        hits = 0;
        misses = 0;
        overwrites = 0;
        stores = 0;
    }
}
