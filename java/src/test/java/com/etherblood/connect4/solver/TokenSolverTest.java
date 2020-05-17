package com.etherblood.connect4.solver;

import com.etherblood.connect4.BoardSettings;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Philipp
 */
public class TokenSolverTest {

    @Test
    public void testSolveVerbose() {
        Map<BoardSettings, Integer> gameResults = new LinkedHashMap<>();
        gameResults.put(new BoardSettings(4, 4), TokenSolver.DRAW_SCORE);
        gameResults.put(new BoardSettings(5, 4), TokenSolver.DRAW_SCORE);
        gameResults.put(new BoardSettings(6, 4), TokenSolver.LOSS_SCORE);
        gameResults.put(new BoardSettings(7, 4), TokenSolver.DRAW_SCORE);
        gameResults.put(new BoardSettings(8, 4), TokenSolver.LOSS_SCORE);
//        gameResults.put(new BoardSettings(9, 4), TokenSolver.LOSS_SCORE);
//        gameResults.put(new BoardSettings(10, 4), TokenSolver.LOSS_SCORE);
//        gameResults.put(new BoardSettings(11, 4), TokenSolver.LOSS_SCORE);

        gameResults.put(new BoardSettings(4, 5), TokenSolver.DRAW_SCORE);
        gameResults.put(new BoardSettings(5, 5), TokenSolver.DRAW_SCORE);
        gameResults.put(new BoardSettings(6, 5), TokenSolver.DRAW_SCORE);
        gameResults.put(new BoardSettings(7, 5), TokenSolver.DRAW_SCORE);
//        gameResults.put(new BoardSettings(8, 5), TokenSolver.WIN_SCORE);
//        gameResults.put(new BoardSettings(9, 5), TokenSolver.WIN_SCORE);
//        gameResults.put(new BoardSettings(10, 5), TokenSolver.WIN_SCORE);

        gameResults.put(new BoardSettings(4, 6), TokenSolver.DRAW_SCORE);
        gameResults.put(new BoardSettings(5, 6), TokenSolver.DRAW_SCORE);
        gameResults.put(new BoardSettings(6, 6), TokenSolver.LOSS_SCORE);
        gameResults.put(new BoardSettings(7, 6), TokenSolver.WIN_SCORE);
//        gameResults.put(new BoardSettings(8, 6), TokenSolver.LOSS_SCORE);
//        gameResults.put(new BoardSettings(9, 6), TokenSolver.LOSS_SCORE);

        gameResults.put(new BoardSettings(4, 7), TokenSolver.DRAW_SCORE);
        gameResults.put(new BoardSettings(5, 7), TokenSolver.DRAW_SCORE);
//        gameResults.put(new BoardSettings(6, 7), TokenSolver.WIN_SCORE);
//        gameResults.put(new BoardSettings(7, 7), TokenSolver.DRAW_SCORE);
//        gameResults.put(new BoardSettings(8, 7), TokenSolver.WIN_SCORE);

        gameResults.put(new BoardSettings(4, 8), TokenSolver.DRAW_SCORE);
        gameResults.put(new BoardSettings(5, 8), TokenSolver.DRAW_SCORE);
//        gameResults.put(new BoardSettings(6, 8), TokenSolver.LOSS_SCORE);
//        gameResults.put(new BoardSettings(7, 8), TokenSolver.WIN_SCORE);
//        gameResults.put(new BoardSettings(8, 8), TokenSolver.LOSS_SCORE);

        gameResults.put(new BoardSettings(4, 9), TokenSolver.DRAW_SCORE);
//        gameResults.put(new BoardSettings(5, 9), TokenSolver.DRAW_SCORE);
//        gameResults.put(new BoardSettings(6, 9), TokenSolver.WIN_SCORE);
        
        gameResults.put(new BoardSettings(4, 10), TokenSolver.DRAW_SCORE);
//        gameResults.put(new BoardSettings(5, 10), TokenSolver.DRAW_SCORE);
        
        gameResults.put(new BoardSettings(4, 11), TokenSolver.DRAW_SCORE);

        for (Map.Entry<BoardSettings, Integer> entry : gameResults.entrySet()) {
            BoardSettings settings = entry.getKey();
            int expectedSolution = entry.getValue();
            TranspositionTable oddTable = new TwoBig1TranspositionTable(1 << 27, settings);
            TranspositionTable evenTable = new TwoBig1TranspositionTable(1 << 27, settings);
            System.out.println(settings.WIDTH + " x " + settings.HEIGHT);
            TokenSolver solver = new TokenSolver(settings, oddTable, evenTable);
            int solution = solver.solveVerbose(0, 0);
            assertEquals(settings.WIDTH + " x " + settings.HEIGHT, expectedSolution, solution);
        }
    }

}
