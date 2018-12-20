#pragma once

#include <cstdint>

namespace bits
{
   std::uint64_t toFlag(std::uint32_t);
   std::uint32_t bitCount(std::uint64_t bits);
   std::uint64_t lowestBit(std::uint64_t bits);
   std::uint64_t highestBit(std::uint64_t bits);
}
