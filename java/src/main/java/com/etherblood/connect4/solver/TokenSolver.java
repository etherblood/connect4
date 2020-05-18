package com.etherblood.connect4.solver;

import com.etherblood.connect4.BoardSettings;
import com.etherblood.connect4.Util;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TokenSolver {

    private static final boolean FOLLOW_UP_STRATEGY_TEST_ENABLED = true;
    private static final boolean NO_WINS_REMAINING_TEST_ENABLED = true;

    public static final int WIN_SCORE = 1;
    public static final int DRAW_SCORE = 0;
    public static final int LOSS_SCORE = -1;

    private final BoardSettings board;
    private final TranspositionTable evenTable, oddTable;

    private final long[] ttStats = new long[8];
    private long drawCutoff, lossCutoff, winCutoff, drawLossCutoff, drawWinCutoff;
    private long totalNodes, totalNanos, alphaNodes, betaNodes;
    private final long[] nodesByDepth;

    private final int[] history;
    private final long[] historyUpdates;
    private final long[] alphaBranching;
    private long work;

    public TokenSolver(BoardSettings board, TranspositionTable oddTable, TranspositionTable evenTable) {
        this.oddTable = oddTable;
        this.evenTable = evenTable;
        this.board = board;

        nodesByDepth = new long[board.WIDTH * board.HEIGHT + 1];

        history = new int[board.WIDTH * board.BUFFERED_HEIGHT - 1];
        historyUpdates = new long[board.WIDTH];
        alphaBranching = new long[board.WIDTH + 1];
    }

    public int solveVerbose(long ownTokens, long opponentTokens) {
        System.out.println(Instant.now());
        System.out.println(board.toString(ownTokens, opponentTokens));
        int solution = solve(ownTokens, opponentTokens);
        System.out.println("Solution is: " + Arrays.asList("loss", "draw", "win").get(solution + 1));
        System.out.println(totalNodes + " nodes in " + Util.humanReadableNanos(totalNanos) + " ("
                + (1_000_000 * totalNodes / totalNanos) + "knps)");
        System.out.println();
        System.out.println("alpha nodes: " + alphaNodes);
        System.out.println("beta nodes: " + betaNodes);
        System.out.println("Mean branching: " + totalNodes / (float) (alphaNodes + betaNodes));
        System.out.println("history updates: " + Arrays.toString(historyUpdates));
        double historyUpdatesSum = Arrays.stream(historyUpdates).sum();
        System.out.println("history updates%: " + Arrays.stream(historyUpdates).mapToDouble(x -> x / historyUpdatesSum).mapToObj(x -> Util.toPercentage(x, 2)).collect(Collectors.joining(", ", "[", "]")));
        double parentsSum = 0;
        double childsSum = 0;
        for (int x = 0; x < historyUpdates.length; x++) {
            long value = historyUpdates[x];
            parentsSum += value;
            childsSum += (1 + x) * value;
        }
        System.out.println("Avg beta branching: " + childsSum / parentsSum);
        parentsSum = 0;
        childsSum = 0;
        for (int x = 0; x < alphaBranching.length; x++) {
            long value = alphaBranching[x];
            parentsSum += value;
            childsSum += x * value;
        }
        System.out.println("Avg alpha branching: " + childsSum / parentsSum);
        System.out.println("nodes by depth: " + Arrays.toString(nodesByDepth));
        float[] branchingByDepth = new float[board.WIDTH * board.HEIGHT];
        for (int i = 0; i < branchingByDepth.length; i++) {
            branchingByDepth[i] = (float) nodesByDepth[i + 1] / nodesByDepth[i];
        }
        System.out.println("branching by depth: " + Arrays.toString(branchingByDepth));
        System.out.println();
        System.out.println("Odd table:");
        oddTable.printStats();
        System.out.println();
        System.out.println("Even table:");
        evenTable.printStats();
        System.out.println();
        Map<Integer, String> scoreNames = new LinkedHashMap<>();
        scoreNames.put(TranspositionTable.UNKNOWN_SCORE, "failed");
        scoreNames.put(TranspositionTable.WIN_SCORE, "win");
        scoreNames.put(TranspositionTable.DRAW_SCORE, "draw");
        scoreNames.put(TranspositionTable.LOSS_SCORE, "loss");
        scoreNames.put(TranspositionTable.DRAW_OR_WIN_SCORE, "draw+");
        scoreNames.put(TranspositionTable.DRAW_OR_LOSS_SCORE, "draw-");
        for (Map.Entry<Integer, String> entry : scoreNames.entrySet()) {
            System.out.println(entry.getValue() + " loads: " + ttStats[entry.getKey()]);
        }
        System.out.println();
        System.out.println("win cuts: " + winCutoff);
        System.out.println("loss cuts: " + lossCutoff);
        System.out.println("draw cuts: " + drawCutoff);
        System.out.println("draw+ cuts: " + drawWinCutoff);
        System.out.println("draw- cuts: " + drawLossCutoff);
        return solution;
    }

    public int solve(long ownTokens, long opponentTokens) {
        if (board.canWin(ownTokens, opponentTokens)) {
            return WIN_SCORE;
        }
        resetHistory();
        Arrays.fill(ttStats, 0);
        Arrays.fill(nodesByDepth, 0);
        Arrays.fill(historyUpdates, 0);
        Arrays.fill(alphaBranching, 0);
        drawWinCutoff = 0;
        drawLossCutoff = 0;
        drawCutoff = 0;
        winCutoff = 0;
        lossCutoff = 0;
        alphaNodes = 0;
        betaNodes = 0;
        totalNodes = 0;
        work = 0;
        totalNanos = -System.nanoTime();
        int score = solve(ownTokens, opponentTokens, LOSS_SCORE, WIN_SCORE);
        totalNanos += System.nanoTime();
        return score;
    }

    private void resetHistory() {
        Arrays.fill(history, 0);
        for (int direction : Arrays.asList(board.RIGHT, board.UP, board.RIGHT_DOWN, board.RIGHT_UP)) {
            long fullSquished = board.squish(board.FULL_BOARD, direction);
            while (fullSquished != 0) {
                long squished = Long.lowestOneBit(fullSquished);
                fullSquished ^= squished;
                long items = board.stretch(squished, direction);
                while (items != 0) {
                    int itemIndex = Long.numberOfTrailingZeros(items);
                    long item = 1L << itemIndex;
                    items ^= item;
                    history[itemIndex]++;
                }
            }
        }
    }

    private int solve(final long ownTokens, final long opponentTokens, final int initialAlpha, final int initialBeta) {
        int alpha = initialAlpha;
        int beta = initialBeta;
        totalNodes++;
        nodesByDepth[Long.bitCount(ownTokens | opponentTokens)]++;
        long moves = board.generateMoves(ownTokens, opponentTokens);
        assert !board.isWin(opponentTokens);
        assert !board.canWin(ownTokens, opponentTokens);
        if (moves == 0) {
            return DRAW_SCORE;
        }
        long opponentThreats = board.threats(opponentTokens, ownTokens);
        long forcedMoves = moves & opponentThreats;
        long losingSquares = opponentThreats >>> board.UP;
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
            return -solve(opponentTokens, board.move(ownTokens, forcedMove), -beta, -alpha);
        }
        if (FOLLOW_UP_STRATEGY_TEST_ENABLED && board.IS_HEIGHT_EVEN && Long.bitCount(moves & board.ODD_INDEX_ROWS) == 1) {
            if ((opponentThreats & board.EVEN_INDEX_ROWS) == 0) {
                long opponentEvenFill = opponentTokens | (~ownTokens & board.EVEN_INDEX_ROWS);
                if (!board.isNonVerticalWin(opponentEvenFill)) {
                    // opponent can't win against follow up strategy
                    if (beta <= DRAW_SCORE) {
                        return DRAW_SCORE;
                    }
                    long ownOddFill = ownTokens | (~opponentTokens & board.ODD_INDEX_ROWS);
                    if (board.isNonVerticalWin(ownOddFill)) {
                        return WIN_SCORE;
                    }
                    alpha = DRAW_SCORE;
                }
            }
        }
        if (NO_WINS_REMAINING_TEST_ENABLED && beta > DRAW_SCORE) {
            if (opponentThreats == 0) {
                if (!board.isNonVerticalWin(~opponentTokens & board.FULL_BOARD)) {
                    // can't win anymore
                    if (alpha >= DRAW_SCORE) {
                        return DRAW_SCORE;
                    }
                    beta = DRAW_SCORE;
                }
            }
        }
        long id = board.id(ownTokens, opponentTokens);
        long mirroredId = board.mirror(id);
        assert board.mirror(mirroredId) == id;
        // losing moves won't improve alpha and can be pruned
        long reducedMoves = moves & ~losingSquares;
        if (id == mirroredId) {
            // symmetric moves won't improve over their counterpart and can be pruned
            reducedMoves &= board.LEFT_SIDE | board.CENTER_BUFFERED_COLUMN;
        }
        if (reducedMoves == Long.lowestOneBit(reducedMoves)) {
            if (reducedMoves == 0) {
                // all moves are losing and were skipped
                return LOSS_SCORE;
            }
            // only 1 move, skip TT
            return -solve(opponentTokens, board.move(ownTokens, reducedMoves), -beta, -alpha);
        }

        int player = Long.bitCount(board.occupied(ownTokens, opponentTokens)) & 1;
        TranspositionTable table = player != 0 ? oddTable : evenTable;
        long symmetricId = Math.min(id, mirroredId);
        int entryScore = table.load(symmetricId);
        long workStart = work;
        work++;
        ttStats[entryScore]++;
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
            case TranspositionTable.DRAW_OR_WIN_SCORE:
                if (DRAW_SCORE >= beta) {
                    drawWinCutoff++;
                    return DRAW_SCORE;
                }
                alpha = DRAW_SCORE;
                break;
            case TranspositionTable.DRAW_OR_LOSS_SCORE:
                if (DRAW_SCORE <= alpha) {
                    drawLossCutoff++;
                    return DRAW_SCORE;
                }
                beta = DRAW_SCORE;
                break;
            default:
                throw new AssertionError(entryScore);
        }

        long movesIterator = reducedMoves;
        while (movesIterator != 0) {
            long move = findBestHistoryMove(movesIterator);
            int score = -solve(opponentTokens, board.move(ownTokens, move), -beta, -alpha);
            if (score > alpha) {
                if (score >= beta) {
                    if (beta == WIN_SCORE) {
                        table.store(symmetricId, work - workStart, TranspositionTable.WIN_SCORE);
                    } else if (initialBeta == WIN_SCORE) {
                        table.store(symmetricId, work - workStart, TranspositionTable.DRAW_SCORE);
                    } else {
                        table.store(symmetricId, work - workStart, entryScore & TranspositionTable.DRAW_OR_WIN_SCORE);
                    }
                    updateHistory(reducedMoves ^ movesIterator, move);
                    betaNodes++;
                    return beta;
                }
                alpha = score;
            }
            movesIterator ^= move;
        }
        if (alpha == LOSS_SCORE) {
            table.store(symmetricId, work - workStart, TranspositionTable.LOSS_SCORE);
        } else if (initialAlpha == LOSS_SCORE) {
            table.store(symmetricId, work - workStart, TranspositionTable.DRAW_SCORE);
        } else {
            table.store(symmetricId, work - workStart, entryScore & TranspositionTable.DRAW_OR_LOSS_SCORE);
        }
        alphaBranching[Long.bitCount(reducedMoves)]++;
        alphaNodes++;
        return alpha;
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
        historyUpdates[weakCount]++;
        history[Long.numberOfTrailingZeros(goodMove)] += weakCount;
        while (weakMoves != 0) {
            history[Long.numberOfTrailingZeros(weakMoves)]--;
            weakMoves &= weakMoves - 1;
        }
    }

}
