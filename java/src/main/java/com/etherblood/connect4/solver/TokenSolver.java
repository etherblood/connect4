package com.etherblood.connect4.solver;

import com.etherblood.connect4.TokenUtil;
import com.etherblood.connect4.Util;
import java.time.Instant;
import java.util.Arrays;

public class TokenSolver extends TokenUtil {

	private static final boolean TT_STATS_ENABLED = false;
	private static final boolean FOLLOW_UP_STRATEGY_TEST_ENABLED = true;
	private static final boolean NO_WINS_REMAINING_TEST_ENABLED = true;

	private static final int WIN_SCORE = 1;
	private static final int DRAW_SCORE = 0;
	private static final int LOSS_SCORE = -1;

	public long[] ttStats = new long[6];
	public long drawLossCutoff, drawWinCutoff;
	public long totalNodes, totalNanos;
	public final SolverTable table;
	private final int[] history = new int[TokenUtil.WIDTH * TokenUtil.BUFFERED_HEIGHT];

	public TokenSolver(SolverTable table) {
		this.table = table;
	}

	public static void main(String[] args) {
		TokenSolver solver = new TokenSolver(new SolverTable(28));
		long ownTokens = 0;
		long opponentTokens = 0;
		System.out.println(Instant.now());
		System.out.println(toString(ownTokens, opponentTokens));
		System.out.println("Solution is: " + solver.solve(ownTokens, opponentTokens));
		System.out.println(solver.totalNodes + " nodes in " + Util.humanReadableNanos(solver.totalNanos) + " ("
				+ (1_000_000 * solver.totalNodes / solver.totalNanos) + "knps)");
		System.out.println();
		solver.table.printStats();
		if (TT_STATS_ENABLED) {
			String[] scoreNames = { "empty", "win", "draw", "loss", "draw+", "draw-" };
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
		int score = solve(ownTokens, opponentTokens, LOSS_SCORE, WIN_SCORE);
		totalNanos += System.nanoTime();
		return score;
	}

	private int solve(long ownTokens, long opponentTokens, int alpha, int beta) {
		totalNodes++;
		long moves = generateMoves(ownTokens, opponentTokens);
		assert !isWin(opponentTokens);
		assert !canWin(ownTokens, moves);
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
		moves &= ~losingSquares;// losing moves won't improve alpha and can be skipped
		if (moves == Long.lowestOneBit(moves)) {
			if (moves == 0) {
				if (isBoardFull(ownTokens, opponentTokens)) {
					return DRAW_SCORE;
				}
				// all moves are losing and were skipped
				return LOSS_SCORE;
			}
			// only 1 move, skip TT
			return -solve(opponentTokens, move(ownTokens, moves), -beta, -alpha);
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

		boolean useTT = (Long.bitCount(occupied(ownTokens, opponentTokens)) & 1) != 0;
		long id = useTT ? hash(ownTokens, opponentTokens) : 0;
		int entryScore = useTT ? table.load(id) : SolverTable.UNKNOWN_SCORE;
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
		boolean symmetrical = isSymmetrical(ownTokens) && isSymmetrical(opponentTokens);
		if (symmetrical) {
			moves &= TokenUtil.LEFT_SIDE | TokenUtil.CENTER_COLUMN;
		}
		try {
			long movesIterator = moves;
			while (movesIterator != 0) {
				long move = findBestMove(movesIterator);
				movesIterator ^= move;
				int score = -solve(opponentTokens, move(ownTokens, move), -beta, -alpha);
				if (score > alpha) {
					if (score >= beta) {
						nextEntryScore = score == WIN_SCORE ? SolverTable.WIN_SCORE : SolverTable.DRAW_WIN_SCORE;
						updateHistory(moves ^ movesIterator, move);
						return score;
					}
					alpha = score;
					nextEntryScore = DRAW_SCORE;
				}
			}
			return alpha;
		} finally {
			if (useTT && entryScore != nextEntryScore) {
				table.store(id, nextEntryScore);
			}
		}
	}

	private long findBestMove(long moves) {
		long bestMove = 0;
		int bestScore = Integer.MIN_VALUE;
		while (moves != 0) {
			int index = Long.numberOfTrailingZeros(moves);
			int nextScore = history[index];
			long nextMove = Util.Long.toFlag(index);
			moves ^= nextMove;
			if (nextScore > bestScore) {
				bestMove = nextMove;
				bestScore = nextScore;
			}
		}
		return bestMove;
	}

	private void updateHistory(long weakMoves, long goodMove) {
		history[Long.numberOfTrailingZeros(goodMove)] += Long.bitCount(weakMoves);
		while (weakMoves != 0) {
			history[Long.numberOfTrailingZeros(weakMoves)]--;
			weakMoves &= weakMoves - 1;
		}
	}

}
