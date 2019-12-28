#include "TokenUtil.h"
#include <iostream>
#include <intrin.h>

static bitboard calcWinCheckPattern(long index)
{
	bitboard pattern = TokenUtil::EMPTY;
	for (long y = 0; y < TokenUtil::HEIGHT; y++)
	{
		for (long x = 0; x < TokenUtil::WIDTH; x++)
		{
			if (index == ((x + 2 * y) & 3))
			{
				pattern |= TokenUtil::nthBit(TokenUtil::position(x, y));
			}
		}
	}
	return pattern;
}
const bitboard WIN_CHECK_PATTERNS[] = { calcWinCheckPattern(0), calcWinCheckPattern(1), calcWinCheckPattern(2), calcWinCheckPattern(3) };

bitboard TokenUtil::lowestOneBit(bitboard board)
{
	return board & (~board + 1);
}

bitboard TokenUtil::clearLowestOneBit(bitboard board)
{
	return board & (board - 1);
}

unsigned long TokenUtil::bitCount(bitboard board)
{
	return (unsigned long)__popcnt64(board);
}

unsigned long TokenUtil::trailingZeros(bitboard board)
{
	return (unsigned long)_tzcnt_u64(board);
}

bitboard TokenUtil::nthBit(unsigned long position)
{
	return FIRST_BIT << position;
}

bitboard TokenUtil::nBits(unsigned long count)
{
	return nthBit(count) - FIRST_BIT;
}

bool TokenUtil::isSymmetrical(bitboard board)
{
	for (long x = 0; x < WIDTH / 2; x++)
	{
		bitboard left = board >> (x * RIGHT);
		bitboard right = board >> ((WIDTH - x - 1) * RIGHT);
		if (((left ^ right) & COLUMN_0))
		{
			return false;
		}
	}
	return true;
}

bitboard TokenUtil::mirror(bitboard board)
{
	bitboard result = board & CENTER_COLUMN;
	for (long x = 0; x < WIDTH / 2; x++)
	{
		long mirrorX = WIDTH - x - 1;
		long offset = mirrorX - x;

		result |= (board & (COLUMN_0 << (x * RIGHT))) << (offset * RIGHT);
		result |= (board & (COLUMN_0 << (mirrorX * RIGHT))) >> (offset * RIGHT);
	}
	return result;
}

bool TokenUtil::isGameOver(bitboard own, bitboard opp)
{
	return isBoardFull(own, opp) || isWin(opp);
}

bool TokenUtil::isBoardFull(bitboard own, bitboard opp)
{
	return occupied(own, opp) == FULL_BOARD;
}

bool TokenUtil::isWin(bitboard board)
{
	return isNonVerticalWin(board)
		|| squish(board, UP);
}

bool TokenUtil::isNonVerticalWin(bitboard board)
{
	return squish(board, RIGHT_UP)
		|| squish(board, RIGHT_DOWN)
		|| squish(board, RIGHT);
}

bool TokenUtil::canWin(bitboard board, bitboard moves)
{
	for (bitboard pattern : WIN_CHECK_PATTERNS)
	{
		bitboard patternMoves = moves & pattern;
		if (patternMoves && isWin(move(board, patternMoves)))
		{
			return true;
		}
	}
	return false;
}

bitboard TokenUtil::move(bitboard board, bitboard move)
{
	return board | move;
}

bitboard TokenUtil::findAnyWinningMove(bitboard board, bitboard moves)
{
	bitboard squished = squish(move(board, moves), UP);
	if (squished)
	{
		return lowestOneBit(stretch(squished, UP) & moves);
	}
	for (bitboard pattern : WIN_CHECK_PATTERNS)
	{
		bitboard patternMoves = moves & pattern;
		if (patternMoves)
		{
			bitboard moved = move(board, patternMoves);
			squished = squish(moved, RIGHT);
			if (squished)
			{
				return lowestOneBit(stretch(squished, RIGHT) & moves);
			}

			squished = squish(moved, RIGHT_DOWN);
			if (squished)
			{
				return lowestOneBit(stretch(squished, RIGHT_DOWN) & moves);
			}

			squished = squish(moved, RIGHT_UP);
			if (squished)
			{
				return lowestOneBit(stretch(squished, RIGHT_UP) & moves);
			}
		}
	}
	return EMPTY;
}

bitboard TokenUtil::threats(bitboard own, bitboard opp)
{
	bitboard full = occupied(own, opp);
	bitboard free = unoccupied(full);
	bitboard squishedRight = EMPTY, squishedRightDown = EMPTY, squishedRightUp = EMPTY;
	for (bitboard pattern : WIN_CHECK_PATTERNS)
	{
		bitboard ownPattern = move(own, free & pattern);
		squishedRight |= squish(ownPattern, RIGHT);
		squishedRightDown |= squish(ownPattern, RIGHT_DOWN);
		squishedRightUp |= squish(ownPattern, RIGHT_UP);
	}
	bitboard squishedUp = squish(move(own, moves(own, opp)), UP);
	return (stretch(squishedUp, UP)
		| stretch(squishedRight, RIGHT)
		| stretch(squishedRightDown, RIGHT_DOWN)
		| stretch(squishedRightUp, RIGHT_UP))
		& free;
}

bitboard TokenUtil::squish(bitboard board, unsigned long direction)
{
	board &= board << (2 * direction);
	board &= board << direction;
	return board;
}

bitboard TokenUtil::stretch(bitboard board, unsigned long direction)
{
	board |= board >> (2 * direction);
	board |= board >> direction;
	return board;
}

bitboard TokenUtil::moves(bitboard own, bitboard opp)
{
	return  (occupied(own, opp) + ROW_0) & FULL_BOARD;
}

bitboard TokenUtil::occupied(bitboard own, bitboard opp)
{
	return own | opp;
}

bitboard TokenUtil::unoccupied(bitboard occupied)
{
	return FULL_BOARD ^ occupied;
}

bitboard TokenUtil::hash(bitboard own, bitboard opp)
{
	return GOLDEN_MULTIPLIER * id(own, opp);
}

bitboard TokenUtil::id(bitboard own, bitboard opp)
{
	return (occupied(own, opp) + ROW_0) ^ own;
}

unsigned long TokenUtil::position(unsigned long x, unsigned long y)
{
	return x * BUFFERED_HEIGHT + y;
}

std::string TokenUtil::toString(bitboard own, bitboard opp)
{
	std::string str = "";
	for (long y = HEIGHT - 1; y >= 0; y--)
	{
		for (unsigned long x = 0; x < WIDTH; x++)
		{
			bitboard bit = nthBit(position(x, y));
			if (bit & own)
			{
				str.append("[x]");
			}
			else if (bit & opp)
			{
				str.append("[o]");
			}
			else
			{
				str.append("[ ]");
			}
		}
		if (y)
		{
			str.append("\n");
		}
	}
	return str;
}

std::string TokenUtil::toString(bitboard board)
{
	return toString(board, EMPTY);
}
