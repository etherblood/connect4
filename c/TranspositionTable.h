#pragma once

#include <cstdint>

enum ScoreType { UPPER_BOUND, LOWER_BOUND, EXACT };

struct TranspositionEntry
{
	std::uint64_t id;
	std::int32_t score;
	std::uint8_t depth;
	ScoreType type;
};

class TranspositionTable
{
public:
	TranspositionTable(std::uint32_t sizeBase=20);
	~TranspositionTable(void);
	bool TranspositionTable::load(std::uint64_t id, TranspositionEntry *result);
	void TranspositionTable::store(TranspositionEntry entry);
	void TranspositionTable::printStats();
private:
	std::uint32_t sizeBase;
	TranspositionEntry *data;
	std::uint64_t mask;

	std::uint64_t hits;
	std::uint64_t misses;
	std::uint64_t stores;
	std::uint64_t loads;
	std::uint64_t TranspositionTable::index(std::uint64_t id);
};

