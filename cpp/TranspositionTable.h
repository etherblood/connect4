#pragma once
#include <cstdint>
#include "TokenUtil.h"

using bitboard = unsigned long long;
class TranspositionTable
{
public:
    static const unsigned long UNKNOWN_SCORE = 0;
    static const unsigned long WIN_SCORE = 1;
    static const unsigned long DRAW_SCORE = 2;
    static const unsigned long LOSS_SCORE = 3;
    static const unsigned long DRAW_WIN_SCORE = 4;
    static const unsigned long DRAW_LOSS_SCORE = 5;

    unsigned long load(bitboard hash);
    void store(bitboard hash, unsigned long score);
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

