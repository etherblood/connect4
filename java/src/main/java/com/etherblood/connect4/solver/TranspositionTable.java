package com.etherblood.connect4.solver;

/**
 *
 * @author Philipp
 */
public interface TranspositionTable {

    static final int UNKNOWN_SCORE = 0;
    static final int WIN_SCORE = 1;
    static final int DRAW_SCORE = 2;
    static final int LOSS_SCORE = 3;
    static final int DRAW_WIN_SCORE = 4;
    static final int DRAW_LOSS_SCORE = 5;
    
    int load(long id);
    void store(long id, int work, int score);
    void printStats();
    void clear();
}
