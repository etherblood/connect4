#include "stdafx.h"
#include "bits.h"
#include <bitset>


namespace bits
{
	std::uint64_t toFlag(std::uint32_t index)
	{
		return 1ULL << index;
	}

	std::uint32_t bitCount(std::uint64_t bits)
	{
		return std::bitset<64>(bits).count();
	}

	std::uint64_t lowestBit(std::uint64_t bits)
	{
		return bits & (~bits + 1);
	}

	std::uint64_t highestBit(std::uint64_t bits)
	{
		bits |= bits >>  1;
		bits |= bits >>  2;
		bits |= bits >>  4;
		bits |= bits >>  8;
		bits |= bits >> 16;
		bits |= bits >> 32;
		return bits - (bits >> 1);
	}
}
