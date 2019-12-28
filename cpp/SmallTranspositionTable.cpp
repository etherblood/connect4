#include "SmallTranspositionTable.h"


uint32_t SmallTranspositionTable::index(bitboard hash)
{
	return (uint32_t)hash & INDEX_MASK;
}

TranspositionTable::Score SmallTranspositionTable::load(bitboard hash)
{
	uint32_t index = SmallTranspositionTable::index(hash);
	bitboard rawEntry = data[index];
	bitboard score = rawEntry ^ hash;
	if (((rawEntry ^ hash) & ID_MASK) == 0)
	{
		return static_cast<TranspositionTable::Score>(rawEntry & SCORE_MASK);
	}
	return TranspositionTable::Score::UNKNOWN;
}

void SmallTranspositionTable::store(bitboard hash, TranspositionTable::Score score)
{
	uint32_t index = SmallTranspositionTable::index(hash);
	data[index] = (static_cast<int>(score) & SCORE_MASK) | (hash & ID_MASK);
}
