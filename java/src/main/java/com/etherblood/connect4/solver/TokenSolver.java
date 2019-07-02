package com.etherblood.connect4.solver;

import com.etherblood.connect4.TokenUtil;
import com.etherblood.connect4.Util;
import java.util.Arrays;

public class TokenSolver extends TokenUtil {

    private static final int WIN_SCORE = 1;
    private static final int DRAW_SCORE = 0;
    private static final int LOSS_SCORE = -1;
    private static final long[] MOVE_ORDER_DEFAULT;
    private static final long[] MOVE_ORDER_SYMMETRICAL;

    public long[] ttStats = new long[6];
    public long drawLossCutoff, drawWinCutoff;
    public long totalNodes, totalNanos;
    public final SolverTable table;

    static {
        //fill columns asap to reduce branching factor by playing topmost moves first
        MOVE_ORDER_DEFAULT = new long[HEIGHT];
        for (int y = 0; y < HEIGHT; y++) {
            long mask = X_AXIS << (y * UP);
            MOVE_ORDER_DEFAULT[HEIGHT - y - 1] = mask;
        }

        if (CENTER_COLUMN != 0) {
            //fill center column first since it keeps symmetry (low branching)
            MOVE_ORDER_SYMMETRICAL = new long[HEIGHT + 1];
            MOVE_ORDER_SYMMETRICAL[0] = CENTER_COLUMN;
            for (int y = 0; y < HEIGHT; y++) {
                MOVE_ORDER_SYMMETRICAL[y + 1] = MOVE_ORDER_DEFAULT[y] & LEFT_SIDE;//skip right side
            }
        } else {
            MOVE_ORDER_SYMMETRICAL = new long[HEIGHT];
            for (int y = 0; y < HEIGHT; y++) {
                MOVE_ORDER_SYMMETRICAL[y] = MOVE_ORDER_DEFAULT[y] & LEFT_SIDE;//skip right side
            }
        }
    }

    public TokenSolver(SolverTable table) {
        this.table = table;
    }

    public static void main(String[] args) {
        TokenSolver solver = new TokenSolver(new SolverTable(28));
        long ownTokens = 0;
        long opponentTokens = 0;
        System.out.println(toString(ownTokens, opponentTokens));
        System.out.println("Solution is: " + solver.solve(ownTokens, opponentTokens));
        System.out.println(solver.totalNodes + " nodes in " + Util.humanReadableNanos(solver.totalNanos) + " (" + (1_000_000 * solver.totalNodes / solver.totalNanos) + "knps)");
        System.out.println();
        solver.table.printStats();
        String[] scoreNames = {"empty", "win", "draw", "loss", "draw+", "draw-"};
        for (int i = 0; i < 6; i++) {
            System.out.println(scoreNames[i] + " loads: " + solver.ttStats[i]);
        }
        System.out.println("draw+ cuts: " + solver.drawWinCutoff);
        System.out.println("draw- cuts: " + solver.drawLossCutoff);
    }

    public int solve(long ownTokens, long opponentTokens) {
        Arrays.fill(ttStats, 0);
        drawWinCutoff = 0;
        drawLossCutoff = 0;
        totalNodes = 0;
        totalNanos = -System.nanoTime();
        int score = solve(ownTokens, opponentTokens, LOSS_SCORE, WIN_SCORE);
        totalNanos += System.nanoTime();
        return score;
    }

    private int solve(long ownTokens, long opponentTokens, int alpha, int beta) {
        assert !isWin(opponentTokens);
        totalNodes++;
        if (isBoardFull(ownTokens, opponentTokens)) {
            return DRAW_SCORE;
        }
        long moves = generateMoves(ownTokens, opponentTokens);
        if (canWin(ownTokens, moves)) {
            return WIN_SCORE;
        }

        long forcedMove = findAnyWinningMove(opponentTokens, moves);
        if (forcedMove != 0) {
            return -solve(opponentTokens, move(ownTokens, forcedMove), -beta, -alpha);
        }

        boolean useTT = (Long.bitCount(ownTokens | opponentTokens) & 1) != 0;
        long id = useTT ? hash(ownTokens, opponentTokens) : 0;
        int entryScore = useTT ? table.load(id) : SolverTable.UNKNOWN_SCORE;
        if (useTT) {
            ttStats[entryScore]++;
            switch (entryScore) {
                case SolverTable.UNKNOWN_SCORE:
                    break;
                case SolverTable.DRAW_LOSS_SCORE:
                    if (DRAW_SCORE <= alpha) {
                        drawLossCutoff++;
                        return DRAW_SCORE;
                    }
                    beta = DRAW_SCORE;
                    break;
                case SolverTable.DRAW_WIN_SCORE:
                    if (DRAW_SCORE >= beta) {
                        drawWinCutoff++;
                        return DRAW_SCORE;
                    }
                    alpha = DRAW_SCORE;
                    break;
                case SolverTable.DRAW_SCORE:
                    return DRAW_SCORE;
                case SolverTable.WIN_SCORE:
                    return WIN_SCORE;
                case SolverTable.LOSS_SCORE:
                    return LOSS_SCORE;
                default:
                    throw new AssertionError();
            }
        }

        int nextEntryScore = alpha == LOSS_SCORE ? SolverTable.LOSS_SCORE : SolverTable.DRAW_LOSS_SCORE;
        try {
            boolean symmetrical = isSymmetrical(ownTokens) && isSymmetrical(opponentTokens);
            for (long orderMask : symmetrical ? MOVE_ORDER_SYMMETRICAL : MOVE_ORDER_DEFAULT) {
                long movesIterator = moves & orderMask;
                while (movesIterator != 0) {
                    long move = Long.lowestOneBit(movesIterator);
                    int score = -solve(opponentTokens, move(ownTokens, move), -beta, -alpha);
                    if (score > alpha) {
                        if (score >= beta) {
                            nextEntryScore = score == WIN_SCORE ? SolverTable.WIN_SCORE : SolverTable.DRAW_WIN_SCORE;
                            return score;
                        }
                        alpha = score;
                        nextEntryScore = DRAW_SCORE;
                    }
                    movesIterator ^= move;
                }
            }
            return alpha;
        } finally {
            if (useTT && entryScore != nextEntryScore) {
                table.store(id, nextEntryScore);
            }
        }
    }

}
