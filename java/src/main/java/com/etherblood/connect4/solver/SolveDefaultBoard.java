package com.etherblood.connect4.solver;

import com.etherblood.connect4.BoardSettings;

public class SolveDefaultBoard {

    public static void main(String[] args) {
        BoardSettings settings = new BoardSettings(7, 6);
        long tableSize = 1L << 28;
        TranspositionTable oddTable = new TwoBig1TranspositionTable(tableSize, settings);
        TranspositionTable evenTable = new TwoBig1TranspositionTable(tableSize, settings);
        System.out.println(settings.WIDTH + " x " + settings.HEIGHT);
        TokenSolver solver = new TokenSolver(settings, oddTable, evenTable);
        int solution = solver.solveVerbose(0, 0);
    }
}
