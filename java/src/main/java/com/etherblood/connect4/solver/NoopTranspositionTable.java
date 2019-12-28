package com.etherblood.connect4.solver;

public class NoopTranspositionTable implements TranspositionTable {

    private long stores, loads;

    public NoopTranspositionTable() {
        stores = 0;
        loads = 0;
    }

    @Override
    public int load(long hash) {
        loads++;
        return LargeTranspositionTable.UNKNOWN_SCORE;
    }

    @Override
    public void store(long hash, int score) {
        stores++;
    }

    @Override
    public void printStats() {
        System.out.println("TT-stats");
        System.out.println(" loads: " + loads);
        System.out.println(" stores: " + stores);
    }
}
