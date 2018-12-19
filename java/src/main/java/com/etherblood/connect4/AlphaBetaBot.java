package com.etherblood.connect4;

import com.etherblood.connect4.eval.Evaluation;
import com.etherblood.connect4.transpositions.TableEntry;
import com.etherblood.connect4.transpositions.TranspositionTable;

/**
 *
 * @author Philipp
 */
public class AlphaBetaBot {

    private final Connect4State state;
    private final Evaluation eval;
    private final TranspositionTable table;
    private final int depth;
    private final boolean pvsEnabled, verbose;
    private long searchedNodes = 0;

    public AlphaBetaBot(Connect4State state, Evaluation eval, TranspositionTable table, int depth, boolean pvsEnabled, boolean verbose) {
        this.state = state;
        this.eval = eval;
        this.table = table;
        this.depth = depth;
        this.pvsEnabled = pvsEnabled;
        this.verbose = verbose;
    }

    public long search() {
        long startNanos = System.nanoTime();
        searchedNodes = 1;
        long bestMove = 0;
        int alpha = eval.lossScore() - depth;
        int beta = -alpha;
        boolean foundPV = false;
        long moves = state.availableMoves();
        while (moves != 0) {
            long move = Long.lowestOneBit(moves);//TODO: move order
            moves ^= move;
            state.move(move);
            int score;
            if (pvsEnabled && foundPV) {
                score = -alphabeta(depth - 1, -alpha - 1, -alpha);
                if (alpha < score) {
                    score = -alphabeta(depth - 1, -beta, -alpha);
                }
            } else {
                score = -alphabeta(depth - 1, -beta, -alpha);
            }
            state.unmove(move);
            if (score > alpha) {
                alpha = score;
                bestMove = move;
                foundPV = true;
            }
        }
        if (verbose) {
            long endNanos = System.nanoTime();
            System.out.println("nodes searched: " + searchedNodes + " in " + (endNanos - startNanos) / 1_000_000 + "ms");
            System.out.println("score: " + (state.activePlayer() == 0 ? alpha : -alpha));
            if (table != null) {
                table.printStats();
            }
        }
        return bestMove;
    }

    public int alphabeta(int depth, int alpha, int beta) {
        searchedNodes++;
        if (state.opponentWon()) {
            return eval.lossScore() - depth;
        }
        if (state.isBoardFull()) {
            return eval.drawScore();
        }
        if (depth == 0) {
            return eval.evaluate();
        }
        TableEntry entry = null;
        if (table != null) {
            entry = table.load(state.id());
        }
        if (entry != null && entry.depth >= depth) {
            switch (entry.type) {
                case TableEntry.UPPER_BOUND:
                    beta = entry.score;
                    if (alpha >= beta) {
                        return alpha;
                    }
                    break;
                case TableEntry.LOWER_BOUND:
                    alpha = entry.score;
                    if (alpha >= beta) {
                        return beta;
                    }
                    break;
                case TableEntry.EXACT:
                    return entry.score;
                default:
                    throw new AssertionError(entry.type);

            }
        }
        int type = TableEntry.UPPER_BOUND;
        boolean foundPV = false;
        long moves = state.availableMoves();
        while (moves != 0) {
            long move = Long.lowestOneBit(moves);//TODO: move order
            moves ^= move;
            state.move(move);
            int score;
            if (pvsEnabled && foundPV) {
                score = -alphabeta(depth - 1, -alpha - 1, -alpha);
                if (alpha < score) {
                    score = -alphabeta(depth - 1, -beta, -alpha);
                }
            } else {
                score = -alphabeta(depth - 1, -beta, -alpha);
            }
            state.unmove(move);
            if (score > alpha) {
                if (score >= beta) {
                    alpha = beta;
                    type = TableEntry.LOWER_BOUND;
                    break;
                }
                alpha = score;
                foundPV = true;
                type = TableEntry.EXACT;
            }
        }
        if (table != null) {
            table.storeRaw(state.id(), type, alpha, depth, 1);
        }
        return alpha;
    }
}
