package com.etherblood.connect4.solver;

import com.etherblood.connect4.TokenUtil;

public class TokenSolver {

    private static final int WIN_SCORE = 1;
    private static final int DRAW_SCORE = 0;
    private static final int LOSS_SCORE = -1;

    public long totalNodes;
    public final SolverTable table;

    public TokenSolver(SolverTable table) {
        this.table = table;
    }

    public static void main(String[] args) {
        TokenSolver solver = new TokenSolver(new SolverTable(27));
        System.out.println(solver.solve(0, 0));
        System.out.println(solver.totalNodes);
        System.out.println();
        solver.table.printStats();
    }

    public int solve(long ownTokens, long opponentTokens) {
        System.out.println(System.currentTimeMillis());
        return solve(ownTokens, opponentTokens, LOSS_SCORE, WIN_SCORE);
    }

    public int solve(long ownTokens, long opponentTokens, int alpha, int beta) {
        assert !TokenUtil.isWin(opponentTokens);
        totalNodes++;
//        if (TokenUtil.isWin(opponentTokens)) {
//            return LOSS_SCORE;
//        }
        if (TokenUtil.isBoardFull(ownTokens, opponentTokens)) {
            return DRAW_SCORE;
        }
        long moves = TokenUtil.generateMoves(ownTokens, opponentTokens);
        //find wins
        long movesIterator = moves;
        while (movesIterator != 0) {
            long move = Long.lowestOneBit(movesIterator);
            long moved = TokenUtil.move(ownTokens, move);
            if (TokenUtil.isWin(moved)) {
                return WIN_SCORE;
            }
            movesIterator ^= move;
        }

        //find forced moves
        movesIterator = moves;
        while (movesIterator != 0) {
            long move = Long.lowestOneBit(movesIterator);
            if (TokenUtil.isWin(TokenUtil.move(opponentTokens, move))) {
                return -solve(opponentTokens, TokenUtil.move(ownTokens, move), -beta, -alpha);
            }
            movesIterator ^= move;
        }

        long id = TokenUtil.id(ownTokens, opponentTokens);
        int entryScore = table.load(id);
        switch (entryScore) {
            case SolverTable.UNKNOWN_SCORE:
                break;
            case SolverTable.DRAW_LOSS_SCORE:
                if (DRAW_SCORE <= alpha) {
                    return DRAW_SCORE;
                }
                beta = DRAW_SCORE;
                break;
            case SolverTable.DRAW_WIN_SCORE:
                if (DRAW_SCORE >= beta) {
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

        boolean exact = false;
        //fill columns asap to reduce branching factor by playing topmost moves first
        for (int y = TokenUtil.HEIGHT - 1; y >= 0; y--) {
            movesIterator = moves & (TokenUtil.X_AXIS << (TokenUtil.UP * y));
            while (movesIterator != 0) {
                long move = Long.lowestOneBit(movesIterator);
                int score = -solve(opponentTokens, TokenUtil.move(ownTokens, move), -beta, -alpha);
                if (opponentTokens == 0) {
                    System.out.println(System.currentTimeMillis());
                    System.out.println("Score for move " + (Long.numberOfTrailingZeros(move) / TokenUtil.BUFFERED_HEIGHT) + " is " + score + ".");
                }
                if (score > alpha) {
                    if (score >= beta) {
                        int nextEntryScore = score == WIN_SCORE ? SolverTable.WIN_SCORE : SolverTable.DRAW_WIN_SCORE;
                        if (entryScore != nextEntryScore) {
                            table.store(id, nextEntryScore);
                        }
                        return score;
                    }
                    alpha = score;
                    exact = true;
                }
                movesIterator ^= move;
            }
        }

        int nextEntryScore = alpha == LOSS_SCORE ? SolverTable.LOSS_SCORE : (exact ? SolverTable.DRAW_SCORE : SolverTable.DRAW_LOSS_SCORE);
        if (entryScore != nextEntryScore) {
            table.store(id, nextEntryScore);
        }
        return alpha;
    }

}
