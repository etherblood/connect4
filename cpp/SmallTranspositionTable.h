#pragma once
#include <cstdint>
#include "TranspositionTable.h"

using bitboard = unsigned long long;
class SmallTranspositionTable
{
public:
	unsigned long load(bitboard hash);
	void store(bitboard hash, unsigned long score);

private:
    static const unsigned long SCORE_BITS = 3;
    static const bitboard ID_MASK = ~0ull << SCORE_BITS;
    static const bitboard SCORE_MASK = ~ID_MASK;

    static const uint32_t SIZE_BASE = 8;
    static const uint32_t INDEX_MASK = (1ul << SIZE_BASE) - 1ul;
    bitboard data[1ul << SIZE_BASE];//make sure this fits into L1-Cache

    uint32_t index(bitboard hash);
};

