#pragma once
#include <cstdint>
#include "TokenUtil.h"

using bitboard = unsigned long long;
class TranspositionTable
{
public:
	enum class Score
	{
		UNKNOWN = 0, WIN = 1, DRAW = 2, LOSS = 3, DRAW_WIN = 4, DRAW_LOSS = 5
	};

	Score load(bitboard hash);
	void store(bitboard hash, Score score);
	void prefetch(bitboard hash);

private:
	static const unsigned long SCORE_BITS = 3;
	static const uint32_t SCORE_MASK = (1ul << SCORE_BITS) - 1ul;
	static const uint32_t ID_MASK = ~SCORE_MASK;

	static const uint32_t SIZE_BASE = 28;
	static const uint32_t INDEX_MASK = (1ul << SIZE_BASE) - 1ul;
	uint32_t data[1ul << SIZE_BASE];

	uint32_t index(bitboard hash);
};

