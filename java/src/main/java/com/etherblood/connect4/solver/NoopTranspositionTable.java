package com.etherblood.connect4.solver;

public class NoopTranspositionTable implements TranspositionTable {

    @Override
    public int load(long id) {
        return UNKNOWN_SCORE;
    }

    @Override
    public void store(long id, long work, int score) {

    }

    @Override
    public void printStats() {

    }

    @Override
    public void clear() {

    }

}
