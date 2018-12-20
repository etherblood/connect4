#pragma once

#include <cstdint>
#include "Connect4.h"
#include "TranspositionTable.h"

class AlphabetaBot
{
public:
	AlphabetaBot(std::uint32_t depth=9, bool pvsEnabled=true, TranspositionTable* table=NULL, bool verbose=true);
	~AlphabetaBot(void);
	std::uint64_t search(Connect4* state);
private:
	TranspositionTable* table;
	std::uint32_t depth;
	std::uint64_t nodes;
	bool verbose, pvsEnabled;
	std::int32_t AlphabetaBot::alphabeta(Connect4* state, std::uint32_t depth, std::int32_t alpha, std::int32_t beta);
};


