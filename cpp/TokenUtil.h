#pragma once
#include <string>

using bitboard = unsigned long long;
namespace TokenUtil
{
	bitboard nthBit(unsigned long position);
	bitboard nBits(unsigned long count);
	bitboard lowestOneBit(bitboard board);
	bitboard clearLowestOneBit(bitboard board);
	unsigned long bitCount(bitboard board);
	unsigned long trailingZeros(bitboard board);
	bool isSymmetrical(bitboard board);
	bitboard mirror(bitboard board);
	bool isGameOver(bitboard own, bitboard opp);
	bool isBoardFull(bitboard own, bitboard opp);
	bool isWin(bitboard board);
	bool isNonVerticalWin(bitboard board);
	bool canWin(bitboard board, bitboard moves);
	bitboard move(bitboard board, bitboard move);
	bitboard findAnyWinningMove(bitboard board, bitboard moves);
	bitboard threats(bitboard own, bitboard opp);
	bitboard squish(bitboard board, unsigned long direction);
	bitboard stretch(bitboard board, unsigned long direction);
	bitboard moves(bitboard own, bitboard opp);
	bitboard occupied(bitboard own, bitboard opp);
	bitboard unoccupied(bitboard occupied);
	bitboard hash(bitboard own, bitboard opp);
	bitboard id(bitboard own, bitboard opp);
	unsigned long position(unsigned long x, unsigned long y);
	std::string toString(bitboard own, bitboard opp);
	std::string toString(bitboard board);

	static const bitboard EMPTY = 0ull;
	static const bitboard FIRST_BIT = 1ull;
	static const long WIDTH = 7;
	static const long HEIGHT = 6;
	static const long BUFFERED_HEIGHT = HEIGHT + 1;
	static const unsigned long UP = 1;
	static const unsigned long RIGHT = BUFFERED_HEIGHT;
	static const unsigned long RIGHT_DOWN = RIGHT - UP;
	static const unsigned long RIGHT_UP = RIGHT + UP;

	static const bool IS_HEIGHT_EVEN = !(HEIGHT & 1);

	static const bitboard COLUMN_0 = nBits(HEIGHT);
	static const bitboard BUFFERED_COLUMN_0 = nBits(BUFFERED_HEIGHT);
	static const bitboard ROW_0 = nBits(WIDTH * BUFFERED_HEIGHT) / BUFFERED_COLUMN_0;

	static const bitboard FULL_BOARD = ROW_0 * COLUMN_0;
	static const bitboard LEFT_SIDE = FULL_BOARD >> ((WIDTH / 2 + (WIDTH % 2))* RIGHT);
	static const bitboard CENTER_COLUMN = (WIDTH & 1) * (COLUMN_0 << (WIDTH / 2 * RIGHT));

	static const bitboard EVEN_INDEX_ROWS = (COLUMN_0 / 3) * ROW_0;
	static const bitboard ODD_INDEX_ROWS = (BUFFERED_COLUMN_0 / 3) * ROW_0;

	static const bitboard GOLDEN_MULTIPLIER = 0x9e3779b97f4a7c15ull;
};

