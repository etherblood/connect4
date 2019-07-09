package com.etherblood.connect4.solver;

import com.etherblood.connect4.TokenUtil;
import com.etherblood.connect4.Util;
import java.util.Arrays;

public class TokenSolver extends TokenUtil {

    private static final boolean TT_STATS_ENABLED = false;

    private static final int WIN_SCORE = 1;
    private static final int DRAW_SCORE = 0;
    private static final int LOSS_SCORE = -1;

    public long[] ttStats = new long[6];
    public long drawLossCutoff, drawWinCutoff;
    public long totalNodes, totalNanos;
    public final SolverTable table;

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
        if (TT_STATS_ENABLED) {
            String[] scoreNames = {"empty", "win", "draw", "loss", "draw+", "draw-"};
            for (int i = 0; i < 6; i++) {
                System.out.println(scoreNames[i] + " loads: " + solver.ttStats[i]);
            }
        }
        System.out.println("draw+ cuts: " + solver.drawWinCutoff);
        System.out.println("draw- cuts: " + solver.drawLossCutoff);
    }

    public int solve(long ownTokens, long opponentTokens) {
        if (canWin(ownTokens, generateMoves(ownTokens, opponentTokens))) {
            return WIN_SCORE;
        }
        Arrays.fill(ttStats, 0);
        drawWinCutoff = 0;
        drawLossCutoff = 0;
        totalNodes = 0;
        totalNanos = -System.nanoTime();
        int score = solve(ownTokens, opponentTokens, LOSS_SCORE, WIN_SCORE, 0);
        totalNanos += System.nanoTime();
        return score;
    }

    private int solve(long ownTokens, long opponentTokens, int alpha, int beta, long hash) {
        totalNodes++;
        long moves = generateMoves(ownTokens, opponentTokens);
        assert !isWin(opponentTokens);
        assert !canWin(ownTokens, moves);
        long opponentThreats = threats(opponentTokens, ownTokens);
        long forcedMoves = moves & opponentThreats;
        long losingSquares = opponentThreats >>> UP;
        if (forcedMoves != 0) {
            if ((forcedMoves & losingSquares) != 0) {
                //forced to play a losing move
                return LOSS_SCORE;
            }
            long forcedMove = Long.lowestOneBit(forcedMoves);
            if (forcedMove != forcedMoves) {
                //there are multiple forced moves which can't all be played
                return LOSS_SCORE;
            }
            // search forced move, skip TT
            return -solve(opponentTokens, move(ownTokens, forcedMove), -beta, -alpha, hashMove(hash, forcedMove));
        }
        moves &= ~losingSquares;//losing moves won't improve alpha and can be skipped
        if (moves == Long.lowestOneBit(moves)) {
            if (moves == 0) {
                if (isBoardFull(ownTokens, opponentTokens)) {
                    return DRAW_SCORE;
                }
                //all moves are losing and were skipped
                return LOSS_SCORE;
            }
            //only 1 move, skip TT
            return -solve(opponentTokens, move(ownTokens, moves), -beta, -alpha, hashMove(hash, moves));
        }

        boolean useTT = (Long.bitCount(occupied(ownTokens, opponentTokens)) & 1) == 0;
        int entryScore = useTT ? table.load(hash) : SolverTable.UNKNOWN_SCORE;
        if (useTT) {
            if (TT_STATS_ENABLED) {
                ttStats[entryScore]++;
            }
            switch (entryScore) {
                case SolverTable.UNKNOWN_SCORE:
                    break;
                case SolverTable.WIN_SCORE:
                    return WIN_SCORE;
                case SolverTable.DRAW_SCORE:
                    return DRAW_SCORE;
                case SolverTable.LOSS_SCORE:
                    return LOSS_SCORE;
                case SolverTable.DRAW_WIN_SCORE:
                    if (DRAW_SCORE >= beta) {
                        drawWinCutoff++;
                        return DRAW_SCORE;
                    }
                    alpha = DRAW_SCORE;
                    break;
                case SolverTable.DRAW_LOSS_SCORE:
                    if (DRAW_SCORE <= alpha) {
                        drawLossCutoff++;
                        return DRAW_SCORE;
                    }
                    beta = DRAW_SCORE;
                    break;
                default:
                    throw new AssertionError();
            }
        }

        int nextEntryScore = alpha == LOSS_SCORE ? SolverTable.LOSS_SCORE : SolverTable.DRAW_LOSS_SCORE;
        try {
            boolean symmetrical = isSymmetrical(ownTokens) && isSymmetrical(opponentTokens);
            for (int phase = HEIGHT - (symmetrical ? 0 : 1); phase >= 0; phase--) {
                long movesIterator;
                if (phase == HEIGHT) {
                    movesIterator = moves & CENTER_COLUMN;
                    //mirrored moves will have identical scores and can be skipped
                    moves &= LEFT_SIDE;
                } else {
                    movesIterator = moves & (X_AXIS << (phase * UP));
                }
                while (movesIterator != 0) {
                    long move = Long.lowestOneBit(movesIterator);
                    int score = -solve(opponentTokens, move(ownTokens, move), -beta, -alpha, hashMove(hash, move));
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
                table.store(hash, nextEntryScore);
            }
        }
    }

}
