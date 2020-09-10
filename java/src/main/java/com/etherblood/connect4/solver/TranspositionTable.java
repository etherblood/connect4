package com.etherblood.connect4.solver;

/**
 *
 * @author Philipp
 */
public interface TranspositionTable {

    static final int WIN_SCORE = 1;
    static final int DRAW_SCORE = 2;
    static final int LOSS_SCORE = 4;
    static final int DRAW_OR_WIN_SCORE = DRAW_SCORE | WIN_SCORE;
    static final int DRAW_OR_LOSS_SCORE = LOSS_SCORE | DRAW_SCORE;
    static final int UNKNOWN_SCORE = LOSS_SCORE | DRAW_SCORE | WIN_SCORE;

    int load(long id);

    void store(long id, long work, int score);

    void printStats();

    void clear();

    static String scoreToString(int score) {
        switch (score) {
            case UNKNOWN_SCORE:
                return "unknown";
            case WIN_SCORE:
                return "win";
            case DRAW_SCORE:
                return "draw";
            case LOSS_SCORE:
                return "loss";
            case DRAW_OR_WIN_SCORE:
                return "draw+";
            case DRAW_OR_LOSS_SCORE:
                return "draw-";
            default:
                throw new AssertionError();
        }
    }
}
