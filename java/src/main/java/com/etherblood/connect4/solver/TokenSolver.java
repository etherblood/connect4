package com.etherblood.connect4.solver;

import com.etherblood.connect4.TokenUtil;
import com.etherblood.connect4.Util;

public class TokenSolver {

    private static final int WIN_SCORE = 1;
    private static final int DRAW_SCORE = 0;
    private static final int LOSS_SCORE = -1;

    public long totalNodes, totalNanos;
    public final SolverTable table;

    public TokenSolver(SolverTable table) {
        this.table = table;
    }

    public static void main(String[] args) {
        TokenSolver solver = new TokenSolver(new SolverTable(27));
        long ownTokens = 0;
        long opponentTokens = 0;
        System.out.println(TokenUtil.toString(ownTokens, opponentTokens));
        System.out.println("Solution is: " + solver.solve(ownTokens, opponentTokens));
        System.out.println(solver.totalNodes + " nodes in " + Util.humanReadableNanos(solver.totalNanos) + " (" + (1_000_000 * solver.totalNodes / solver.totalNanos) + "knps)");
        solver.table.printStats();
    }

    public int solve(long ownTokens, long opponentTokens) {
        totalNanos -= System.nanoTime();
        int score = solve(ownTokens, opponentTokens, LOSS_SCORE, WIN_SCORE);
        totalNanos += System.nanoTime();
        return score;
    }

    public int solve(long ownTokens, long opponentTokens, int alpha, int beta) {
        assert !TokenUtil.isWin(opponentTokens);
        totalNodes++;
        if (TokenUtil.isBoardFull(ownTokens, opponentTokens)) {
            return DRAW_SCORE;
        }
        long moves = TokenUtil.generateMoves(ownTokens, opponentTokens);
        if (canWin(ownTokens, moves)) {
            return WIN_SCORE;
        }

        long forcedMove = findAnyWinningMove(opponentTokens, moves);
        if (forcedMove != 0) {
            return -solve(opponentTokens, TokenUtil.move(ownTokens, forcedMove), -beta, -alpha);
        }

        boolean useTT = (Long.bitCount(ownTokens | opponentTokens) & 1) != 0;
        long id = useTT ? TokenUtil.id(ownTokens, opponentTokens) : 0;
        int entryScore = useTT ? table.load(id) : 0;
        if (useTT) {
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
        }

        boolean exact = false;
        if (TokenUtil.isSymmetrical(ownTokens) && TokenUtil.isSymmetrical(opponentTokens)) {
            long movesIterator = moves & TokenUtil.CENTER_COLUMN;
            moves &= ~TokenUtil.CENTER_COLUMN;
            //prune symmetrical moves
            moves &= TokenUtil.LEFT_SIDE;
            while (movesIterator != 0) {
                long move = Long.lowestOneBit(movesIterator);
                int score = -solve(opponentTokens, TokenUtil.move(ownTokens, move), -beta, -alpha);
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

        //fill columns asap to reduce branching factor by playing topmost moves first
        for (int y = TokenUtil.HEIGHT - 1; y >= 0; y--) {
            long movesIterator = moves & (TokenUtil.X_AXIS << (TokenUtil.UP * y));
            while (movesIterator != 0) {
                long move = Long.lowestOneBit(movesIterator);
                int score = -solve(opponentTokens, TokenUtil.move(ownTokens, move), -beta, -alpha);
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

        if (useTT) {
            int nextEntryScore = alpha == LOSS_SCORE ? SolverTable.LOSS_SCORE : (exact ? SolverTable.DRAW_SCORE : SolverTable.DRAW_LOSS_SCORE);
            if (entryScore != nextEntryScore) {
                table.store(id, nextEntryScore);
            }
        }
        return alpha;
    }

    private boolean canWin(long tokens, long moves) {
        for (long pattern : TokenUtil.WIN_CHECK_PATTERNS) {
            long patternMoves = moves & pattern;
            if (patternMoves != 0 && TokenUtil.isWin(TokenUtil.move(tokens, patternMoves))) {
                return true;
            }
        }
        return false;
    }

    public long findAnyWinningMove(long tokens, long moves) {
        long squished = TokenUtil.squish(TokenUtil.move(tokens, moves), TokenUtil.UP);
        if (squished != 0) {
            return Long.lowestOneBit(TokenUtil.stretch(squished, TokenUtil.UP) & moves);
        }
        for (long pattern : TokenUtil.WIN_CHECK_PATTERNS) {
            long patternMoves = moves & pattern;
            if (patternMoves != 0) {
                long moved = TokenUtil.move(tokens, patternMoves);
                squished = TokenUtil.squish(moved, TokenUtil.RIGHT);
                if (squished != 0) {
                    return Long.lowestOneBit(TokenUtil.stretch(squished, TokenUtil.RIGHT) & moves);
                }

                squished = TokenUtil.squish(moved, TokenUtil.RIGHT_DOWN);
                if (squished != 0) {
                    return Long.lowestOneBit(TokenUtil.stretch(squished, TokenUtil.RIGHT_DOWN) & moves);
                }

                squished = TokenUtil.squish(moved, TokenUtil.RIGHT_UP);
                if (squished != 0) {
                    return Long.lowestOneBit(TokenUtil.stretch(squished, TokenUtil.RIGHT_UP) & moves);
                }
            }
        }
        return 0;
    }

}
