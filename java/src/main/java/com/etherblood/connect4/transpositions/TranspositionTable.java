package com.etherblood.connect4.transpositions;

/**
 *
 * @author Philipp
 */
public interface TranspositionTable {

    TableEntry load(long id);

    void storeRaw(long id, int type, int score, int depth, long move);

    void printStats();
}
