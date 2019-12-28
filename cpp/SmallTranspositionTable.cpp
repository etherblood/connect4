#include "SmallTranspositionTable.h"


uint32_t SmallTranspositionTable::index(bitboard hash)
{
	return (uint32_t)hash & INDEX_MASK;
}

unsigned long SmallTranspositionTable::load(bitboard hash)
{
	uint32_t index = SmallTranspositionTable::index(hash);
	bitboard rawEntry = data[index];
	bitboard score = rawEntry ^ hash;
	if (((rawEntry ^ hash) & ID_MASK) == 0)
	{
		return rawEntry & SCORE_MASK;
	}
	return TranspositionTable::UNKNOWN_SCORE;
}

void SmallTranspositionTable::store(bitboard hash, unsigned long score)
{
	uint32_t index = SmallTranspositionTable::index(hash);
	data[index] = (score & SCORE_MASK) | (hash & ID_MASK);
}
