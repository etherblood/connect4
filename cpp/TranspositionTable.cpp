#include "TranspositionTable.h"
#include "TokenUtil.h"
#include <intrin.h>

uint32_t TranspositionTable::index(bitboard hash)
{
	uint32_t upperHash = (uint32_t)(hash >> 32);
	return upperHash & INDEX_MASK;
}

unsigned long TranspositionTable::load(bitboard hash)
{
	uint32_t index = TranspositionTable::index(hash);
	uint32_t rawEntry = data[index];
	if (((rawEntry ^ (uint32_t)hash) & ID_MASK) == 0) {
		return rawEntry & SCORE_MASK;
	}
	return UNKNOWN_SCORE;
}

void TranspositionTable::store(bitboard hash, unsigned long score)
{
	uint32_t index = TranspositionTable::index(hash);
	data[index] = (((uint32_t)score & SCORE_MASK) | ((uint32_t)hash & ID_MASK));
}

void TranspositionTable::prefetch(bitboard hash)
{
	uint32_t index = TranspositionTable::index(hash);
	_mm_prefetch((const char*)(&data[index]), 0);
}
