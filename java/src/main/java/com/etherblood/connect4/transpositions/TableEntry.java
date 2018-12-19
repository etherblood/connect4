package com.etherblood.connect4.transpositions;

/**
 *
 * @author Philipp
 */
public class TableEntry {

    public static final int UPPER_BOUND = 1, LOWER_BOUND = 2, EXACT = 3;

    public long stateId, move;
    public int depth, score, type;
}
