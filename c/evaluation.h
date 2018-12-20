#pragma once

#include <cstdint>
#include "Connect4.h"

namespace evaluation
{
	const std::int32_t LOSS_SCORE = -10000;
	std::int32_t evaluate(Connect4* state);
}

