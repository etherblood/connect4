#include "stdafx.h"
#include "TranspositionTable.h"
#include <iostream>


TranspositionTable::TranspositionTable(std::uint32_t sizeBase)
{
	this->sizeBase = sizeBase;
	this->data = new TranspositionEntry[1 << sizeBase];
	this->mask = (1 << sizeBase) - 1;

	this->hits = 0;
	this->misses = 0;
	this->stores = 0;
	this->loads = 0;
}


TranspositionTable::~TranspositionTable(void)
{
	delete data;
}

std::uint64_t TranspositionTable::index(std::uint64_t id)
{
	std::uint64_t hash = 11400714819323198485ull * id;
	return (hash >> (64 - this->sizeBase)) & this->mask;
}

bool TranspositionTable::load(std::uint64_t id, TranspositionEntry* result)
{
	this->loads += 1;
	std::uint64_t index = this->index(id);
	TranspositionEntry entry = this->data[index];
	if (entry.id == id)
	{
		this->hits += 1;
		*result = entry;
		return true;
	}
	this->misses += 1;
	return false;
}

void TranspositionTable::store(TranspositionEntry entry)
{
	this->stores += 1;
	std::uint64_t index = this->index(entry.id);
	data[index] = entry;
}

void TranspositionTable::printStats()
{
	std::cout << "TT-stats\n hits: " << this->hits << std::endl << " misses: " << this->misses << std::endl << " loads: " << this->loads << std::endl << " stores: " << this->stores << std::endl;
}
