#include "TokenSolver.h"
#include "TokenUtil.h"
#include "TranspositionTable.h"
#include "SmallTranspositionTable.h"
#include <iostream>

#define FOLLOW_UP_STRATEGY_TEST_ENABLED
#define NO_WINS_REMAINING_TEST_ENABLED
#define SYMMETRY_TEST_ENABLED
#define TRANSPOSITION_TABLE_ENABLED
//#define SMALL_TRANSPOSITION_TABLE_ENABLED

const long WIN_SCORE = 1;
const long DRAW_SCORE = 0;
const long LOSS_SCORE = -1;

unsigned long hit = 0, miss = 0;
unsigned long long nodes = 0;

#ifdef TRANSPOSITION_TABLE_ENABLED
TranspositionTable table = TranspositionTable();
#ifdef SMALL_TRANSPOSITION_TABLE_ENABLED
SmallTranspositionTable smallTable = SmallTranspositionTable();
#endif
#endif
long history[TokenUtil::WIDTH * TokenUtil::BUFFERED_HEIGHT - 1] = {};

bitboard findBestHistoryMove(bitboard moves)
{
	bitboard bestMove = TokenUtil::EMPTY;
	long bestScore = LONG_MIN;
	while (moves)
	{
		unsigned long index = TokenUtil::trailingZeros(moves);
		long nextScore = history[index];
		bitboard nextMove = TokenUtil::nthBit(index);
		moves ^= nextMove;
		if (nextScore > bestScore)
		{
			bestMove = nextMove;
			bestScore = nextScore;
		}
	}
	return bestMove;
}

void updateHistory(bitboard weakMoves, bitboard goodMove)
{
	history[TokenUtil::trailingZeros(goodMove)] += TokenUtil::bitCount(weakMoves);
	while (weakMoves) {
		history[TokenUtil::trailingZeros(weakMoves)]--;
		weakMoves = TokenUtil::clearLowestOneBit(weakMoves);
	}
}

long search(bitboard own, bitboard opp, long alpha, long beta)
{
	nodes++;
	bitboard moves = TokenUtil::moves(own, opp);
	if (moves == 0)
	{
		return DRAW_SCORE;
	}
	bitboard oppThreats = TokenUtil::threats(opp, own);
	bitboard forcedMoves = moves & oppThreats;
	bitboard losingSquares = oppThreats >> TokenUtil::UP;
	if (forcedMoves)
	{
		if (forcedMoves & losingSquares)
		{
			//forced to play a losing move
			return LOSS_SCORE;
		}
		bitboard forcedMove = TokenUtil::lowestOneBit(forcedMoves);
		if (forcedMove != forcedMoves)
		{
			//there are multiple forced moves which can't all be played
			return LOSS_SCORE;
		}
		// search forced move
		return -search(opp, TokenUtil::move(own, forcedMove), -beta, -alpha);
	}
	//losing moves won't improve alpha and can be skipped
	moves &= ~losingSquares;
	if (moves == TokenUtil::lowestOneBit(moves))
	{
		if (moves == 0)
		{
			//all moves are losing and were skipped
			return LOSS_SCORE;
		}
		//only 1 move
		return -search(opp, TokenUtil::move(own, moves), -beta, -alpha);
	}

#ifdef FOLLOW_UP_STRATEGY_TEST_ENABLED
	if (TokenUtil::IS_HEIGHT_EVEN && TokenUtil::bitCount(moves & TokenUtil::ODD_INDEX_ROWS) == 1) {
		//test whether follow up strategy ensures at least a draw
		if (!(oppThreats & TokenUtil::EVEN_INDEX_ROWS)) {
			bitboard oppEvenFill = opp | (~own & TokenUtil::EVEN_INDEX_ROWS);
			if (!TokenUtil::isNonVerticalWin(oppEvenFill)) {
				if (beta <= DRAW_SCORE) {
					return DRAW_SCORE;
				}
				bitboard ownOddFill = own | (~opp & TokenUtil::ODD_INDEX_ROWS);
				if (TokenUtil::isNonVerticalWin(ownOddFill)) {
					return WIN_SCORE;
				}
				alpha = DRAW_SCORE;
			}
		}
	}
#endif

#ifdef NO_WINS_REMAINING_TEST_ENABLED
	if (beta > DRAW_SCORE) {
		if (!oppThreats) {
			if (!TokenUtil::isNonVerticalWin(~opp & TokenUtil::FULL_BOARD)) {
				//can't win anymore
				if (alpha >= DRAW_SCORE) {
					return DRAW_SCORE;
				}
				beta = DRAW_SCORE;
			}
		}
	}
#endif

#ifdef SYMMETRY_TEST_ENABLED
	if (TokenUtil::isSymmetrical(own) && TokenUtil::isSymmetrical(opp))
	{
		moves &= TokenUtil::LEFT_SIDE | TokenUtil::CENTER_COLUMN;
		if (TokenUtil::bitCount(moves) == 1)
		{
			//only 1 move
			return -search(opp, TokenUtil::move(own, moves), -beta, -alpha);
		}
	}
#endif

#ifdef TRANSPOSITION_TABLE_ENABLED
	//loading from TT is very expensive, only doing it at odd depths tested to be faster
	bool useMainTable = TokenUtil::bitCount(TokenUtil::occupied(own, opp)) & 1;
	bitboard id = TokenUtil::hash(own, opp);
	TranspositionTable::Score entryScore;
	if (useMainTable)
	{
		entryScore = table.load(id);
	}
	else
	{
#ifdef SMALL_TRANSPOSITION_TABLE_ENABLED
		entryScore = smallTable.load(id);
		if (entryScore == TranspositionTable::UNKNOWN_SCORE)
		{
			miss++;
		}
		else
		{
			hit++;
		}
#else
		entryScore = TranspositionTable::Score::UNKNOWN;
#endif
	}
	switch (entryScore) {
	case TranspositionTable::Score::UNKNOWN:
		break;
	case TranspositionTable::Score::WIN:
		return WIN_SCORE;
	case TranspositionTable::Score::DRAW:
		return DRAW_SCORE;
	case TranspositionTable::Score::LOSS:
		return LOSS_SCORE;
	case TranspositionTable::Score::DRAW_WIN:
		if (DRAW_SCORE >= beta) {
			return DRAW_SCORE;
		}
		alpha = DRAW_SCORE;
		break;
	case TranspositionTable::Score::DRAW_LOSS:
		if (DRAW_SCORE <= alpha) {
			return DRAW_SCORE;
		}
		beta = DRAW_SCORE;
		break;
	default:
		throw;
	}

	TranspositionTable::Score nextEntryScore = alpha == LOSS_SCORE ? TranspositionTable::Score::LOSS : TranspositionTable::Score::DRAW_LOSS;
#endif
	bitboard movesIterator = moves;
	while (movesIterator)
	{
		bitboard move = findBestHistoryMove(movesIterator);
		long score = -search(opp, TokenUtil::move(own, move), -beta, -alpha);
		if (score > alpha)
		{
			if (score >= beta)
			{
				updateHistory(moves ^ movesIterator, move);
#ifdef TRANSPOSITION_TABLE_ENABLED
				nextEntryScore = score == WIN_SCORE ? TranspositionTable::Score::WIN : TranspositionTable::Score::DRAW_WIN;
				if (entryScore != nextEntryScore)
				{
					if (useMainTable)
					{
						table.store(id, nextEntryScore);
					}
#ifdef SMALL_TRANSPOSITION_TABLE_ENABLED
					else
					{
						smallTable.store(id, nextEntryScore);
					}
#endif
				}
#endif
				return score;
			}
#ifdef TRANSPOSITION_TABLE_ENABLED
			nextEntryScore = TranspositionTable::Score::DRAW;
#endif
			alpha = score;
		}

		movesIterator ^= move;
	}
#ifdef TRANSPOSITION_TABLE_ENABLED
	if (entryScore != nextEntryScore)
	{
		if (useMainTable)
		{
			table.store(id, nextEntryScore);
		}
#ifdef SMALL_TRANSPOSITION_TABLE_ENABLED
		else
		{
			smallTable.store(id, nextEntryScore);
		}
#endif
	}
#endif
	return alpha;
}

long TokenSolver::solve(bitboard own, bitboard opp)
{
	if (TokenUtil::canWin(own, TokenUtil::moves(own, opp))) {
		return WIN_SCORE;
	}
	long result = search(own, opp, LOSS_SCORE, WIN_SCORE);
	std::cout << "nodes: " << std::to_string(nodes) << "\n";
	std::cout << std::to_string(hit) << ", " << std::to_string(miss) << "\n";
	return result;
}
