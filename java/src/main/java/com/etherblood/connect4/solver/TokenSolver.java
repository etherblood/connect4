package com.etherblood.connect4.solver;

import com.etherblood.connect4.TokenUtil;
import com.etherblood.connect4.Util;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TokenSolver extends TokenUtil {

    private static final boolean ARRAY_STATS_ENABLED = true;
    private static final boolean FOLLOW_UP_STRATEGY_TEST_ENABLED = true;
    private static final boolean NO_WINS_REMAINING_TEST_ENABLED = true;
    private static final boolean SYMMETRY_TEST_ENABLED = true;

    private static final int WIN_SCORE = 1;
    private static final int DRAW_SCORE = 0;
    private static final int LOSS_SCORE = -1;

    public long[] ttStats = new long[6];
    public long drawCutoff, lossCutoff, winCutoff, drawLossCutoff, drawWinCutoff, drawLossLoads, drawWinLoads;
    public long totalNodes, totalNanos, alphaNodes, betaNodes;
    public final TranspositionTable evenTable, oddTable;
    private final int[] history = new int[TokenUtil.WIDTH * TokenUtil.BUFFERED_HEIGHT - 1];
    private final int[] historyUpdates = new int[WIDTH];

    public TokenSolver(TranspositionTable oddTable, TranspositionTable evenTable) {
        this.oddTable = oddTable;
        this.evenTable = evenTable;
    }

    public static void main(String[] args) {
        //table sizes chosen carefully as pseudo seeds, they seem to perform well for 7x6
        TokenSolver solver = new TokenSolver(new TranspositionTableImpl(1073741717L), new TranspositionTableImpl(1073741651L));
        for (int i = 0; i < 1; i++) {
            solve(solver);
            solver.oddTable.clear();
            solver.evenTable.clear();
        }
    }

    private static void solve(TokenSolver solver) {
        long ownTokens = 0;
        long opponentTokens = 0;
        System.out.println(Instant.now());
        System.out.println(toString(ownTokens, opponentTokens));
        System.out.println("Solution is: " + solver.solve(ownTokens, opponentTokens));
        System.out.println(solver.totalNodes + " nodes in " + Util.humanReadableNanos(solver.totalNanos) + " ("
                + (1_000_000 * solver.totalNodes / solver.totalNanos) + "knps)");
        System.out.println();
        System.out.println("alpha nodes: " + solver.alphaNodes);
        System.out.println("beta nodes: " + solver.betaNodes);
        System.out.println("history updates: " + Arrays.toString(solver.historyUpdates));
        System.out.println("history updates%: " + Arrays.stream(solver.historyUpdates).mapToDouble(x -> (double) x / solver.betaNodes).mapToObj(x -> toPercentage(x, 2)).collect(Collectors.joining(", ", "[", "]")));
        System.out.println();
        System.out.println("Odd table:");
        solver.oddTable.printStats();
        System.out.println();
        System.out.println("Even table:");
        solver.evenTable.printStats();
        System.out.println();
        if (ARRAY_STATS_ENABLED) {
            String[] scoreNames = {"empty", "win", "draw", "loss", "draw+", "draw-"};
            for (int i = 0; i < 6; i++) {
                System.out.println(scoreNames[i] + " loads: " + solver.ttStats[i]);
            }
            System.out.println();
        }
        System.out.println("win cuts: " + solver.winCutoff);
        System.out.println("loss cuts: " + solver.lossCutoff);
        System.out.println("draw cuts: " + solver.drawCutoff);
        System.out.println("draw+ cuts: " + solver.drawWinCutoff);
        System.out.println("draw- cuts: " + solver.drawLossCutoff);
    }

    private static String toPercentage(double n, int digits) {
        return String.format("%." + digits + "f", n * 100) + "%";
    }

    public int solve(long ownTokens, long opponentTokens) {
        if (canWin(ownTokens, opponentTokens)) {
            return WIN_SCORE;
        }
        resetHistory();
        Arrays.fill(ttStats, 0);
        drawWinCutoff = 0;
        drawLossCutoff = 0;
        drawCutoff = 0;
        winCutoff = 0;
        lossCutoff = 0;
        alphaNodes = 0;
        betaNodes = 0;
        totalNodes = 0;
        totalNanos = -System.nanoTime();
        int score = solve(ownTokens, opponentTokens, LOSS_SCORE, WIN_SCORE);
        totalNanos += System.nanoTime();
        return score;
    }

    private void resetHistory() {
        Arrays.fill(historyUpdates, 0);
        for (int x = 0; x < TokenUtil.WIDTH; x++) {
            for (int y = 0; y < TokenUtil.HEIGHT; y++) {
                int index = TokenUtil.index(x, y);
                history[index] = ((2 * y) - Math.abs((WIDTH - 1) - 2 * x) + (WIDTH - 1)) / 2;
            }
        }
    }

    private int solve(long ownTokens, long opponentTokens, int alpha, int beta) {
        totalNodes++;
        long moves = generateMoves(ownTokens, opponentTokens);
        assert !isWin(opponentTokens);
        assert !canWin(ownTokens, opponentTokens);
        if (moves == 0) {
            return DRAW_SCORE;
        }
        long opponentThreats = threats(opponentTokens, ownTokens);
        long forcedMoves = moves & opponentThreats;
        long losingSquares = opponentThreats >>> UP;
        if (forcedMoves != 0) {
            if ((forcedMoves & losingSquares) != 0) {
                // forced to play a losing move
                return LOSS_SCORE;
            }
            long forcedMove = Long.lowestOneBit(forcedMoves);
            if (forcedMove != forcedMoves) {
                // there are multiple forced moves which can't all be played
                return LOSS_SCORE;
            }
            // search forced move, skip TT
            return -solve(opponentTokens, move(ownTokens, forcedMove), -beta, -alpha);
        }
        if (FOLLOW_UP_STRATEGY_TEST_ENABLED && IS_HEIGHT_EVEN && Long.bitCount(moves & ODD_INDEX_ROWS) == 1) {
            // test whether follow up strategy ensures at least a draw
            if ((opponentThreats & EVEN_INDEX_ROWS) == 0) {
                long opponentEvenFill = opponentTokens | (~ownTokens & EVEN_INDEX_ROWS);
                if (!isNonVerticalWin(opponentEvenFill)) {
                    if (beta <= DRAW_SCORE) {
                        return DRAW_SCORE;
                    }
                    long ownOddFill = ownTokens | (~opponentTokens & ODD_INDEX_ROWS);
                    if (isNonVerticalWin(ownOddFill)) {
                        return WIN_SCORE;
                    }
                    alpha = DRAW_SCORE;
                }
            }
        }
        if (NO_WINS_REMAINING_TEST_ENABLED && beta > DRAW_SCORE) {
            if (opponentThreats == 0) {
                if (!isNonVerticalWin(~opponentTokens & FULL_BOARD)) {
                    // can't win anymore
                    if (alpha >= DRAW_SCORE) {
                        return DRAW_SCORE;
                    }
                    beta = DRAW_SCORE;
                }
            }
        }
        long id = TokenUtil.id(ownTokens, opponentTokens);
        long mirroredId = TokenUtil.mirror(id);
        // losing moves won't improve alpha and can be pruned
        long prunedMoves = moves & ~losingSquares;
        if (SYMMETRY_TEST_ENABLED && id == mirroredId) {
            // symmetric moves won't improve over their counterpart and can be pruned
            prunedMoves &= TokenUtil.LEFT_SIDE | TokenUtil.CENTER_COLUMN;
        }
        if (prunedMoves == Long.lowestOneBit(prunedMoves)) {
            if (prunedMoves == 0) {
                // all moves are losing and were skipped
                return LOSS_SCORE;
            }
            // only 1 move, skip TT
            return -solve(opponentTokens, move(ownTokens, prunedMoves), -beta, -alpha);
        }

        int player = Long.bitCount(occupied(ownTokens, opponentTokens)) & 1;
        TranspositionTable table = player != 0 ? oddTable : evenTable;
        long symmetricId = Math.min(id, mirroredId);
        int entryScore = table.load(symmetricId);
        if (ARRAY_STATS_ENABLED) {
            ttStats[entryScore]++;
        }
        switch (entryScore) {
            case TranspositionTable.UNKNOWN_SCORE:
                break;
            case TranspositionTable.WIN_SCORE:
                winCutoff++;
                return WIN_SCORE;
            case TranspositionTable.DRAW_SCORE:
                drawCutoff++;
                return DRAW_SCORE;
            case TranspositionTable.LOSS_SCORE:
                lossCutoff++;
                return LOSS_SCORE;
            case TranspositionTable.DRAW_WIN_SCORE:
                if (DRAW_SCORE >= beta) {
                    drawWinCutoff++;
                    return DRAW_SCORE;
                }
                alpha = DRAW_SCORE;
                break;
            case TranspositionTable.DRAW_LOSS_SCORE:
                if (DRAW_SCORE <= alpha) {
                    drawLossCutoff++;
                    return DRAW_SCORE;
                }
                beta = DRAW_SCORE;
                break;
            default:
                throw new AssertionError(Integer.toString(entryScore));
        }

        int nextEntryScore = alpha == LOSS_SCORE ? TranspositionTable.LOSS_SCORE : TranspositionTable.DRAW_LOSS_SCORE;
        try {
            long movesIterator = prunedMoves;
            while (movesIterator != 0) {
                long move = findBestHistoryMove(movesIterator);
                int score = -solve(opponentTokens, move(ownTokens, move), -beta, -alpha);
                if (score > alpha) {
                    if (score >= beta) {
                        nextEntryScore = score == WIN_SCORE ? TranspositionTable.WIN_SCORE : TranspositionTable.DRAW_WIN_SCORE;
                        updateHistory(prunedMoves ^ movesIterator, move);
                        betaNodes++;
                        return score;
                    }
                    alpha = score;
                    nextEntryScore = TranspositionTable.DRAW_SCORE;
                }
                movesIterator ^= move;
            }
            alphaNodes++;
            return alpha;
        } finally {
            if (entryScore != nextEntryScore) {
                table.store(symmetricId, nextEntryScore);
            }
        }
    }

    private long findBestHistoryMove(long moves) {
        long bestMove = 0;
        int bestScore = Integer.MIN_VALUE;
        while (moves != 0) {
            int index = Long.numberOfTrailingZeros(moves);
            int nextScore = history[index];
            long nextMove = Util.toLongFlag(index);
            moves ^= nextMove;
            if (nextScore > bestScore) {
                bestMove = nextMove;
                bestScore = nextScore;
            }
        }
        return bestMove;
    }

    private void updateHistory(long weakMoves, long goodMove) {
        int weakCount = Long.bitCount(weakMoves);
        if (ARRAY_STATS_ENABLED) {
            historyUpdates[weakCount]++;
        }
        history[Long.numberOfTrailingZeros(goodMove)] += weakCount;
        while (weakMoves != 0) {
            history[Long.numberOfTrailingZeros(weakMoves)]--;
            weakMoves &= weakMoves - 1;
        }
    }

}
