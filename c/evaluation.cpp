#include "stdafx.h"
#include "evaluation.h"
#include "bits.h"

namespace evaluation
{
	std::int32_t SIDE_TO_MOVE = 10;
	std::int32_t FACTOR_3 = 18;

	std::uint64_t shifts4(Connect4* state, std::uint64_t mask, std::uint32_t direction)
	{
		mask &= mask << (2 * state->winShifts[direction]);
		mask &= state->winMasks[direction];
		mask &= mask << state->winShifts[direction];
		return mask;
	}

	std::uint64_t strech4(Connect4* state, std::uint64_t mask, std::uint32_t direction)
	{
		mask |= mask >> (2 * state->winShifts[direction]);
		mask |= mask >> state->winShifts[direction];
		return mask;
	}

	std::int32_t evaluate(Connect4* state)
	{
		std::uint32_t current_player = state->activePlayer();
		std::uint32_t opponent = state->opponent();
		std::uint64_t own_tokens = state->playerTokens[current_player];
		std::uint64_t opponent_tokens = state->playerTokens[opponent];

		std::int32_t score = SIDE_TO_MOVE;
		for (int i = 0; i < 4; i++)
		{
			std::uint64_t win_positions = shifts4(state, state->fullBoard & ~opponent_tokens, i);
			std::uint64_t win_fields = strech4(state, win_positions, i);
			std::uint64_t tokens = own_tokens;
			tokens &= tokens << state->winShifts[i];
			score += bits::bitCount(win_fields & tokens);
			tokens &= tokens << state->winShifts[i];
			score += FACTOR_3 * bits::bitCount(win_fields & tokens);

			win_positions = shifts4(state, state->fullBoard & ~own_tokens, i);
			win_fields = strech4(state, win_positions, i);
			tokens = opponent_tokens;
			tokens &= tokens << state->winShifts[i];
			score -= bits::bitCount(win_fields & tokens);
			tokens &= tokens << state->winShifts[i];
			score -= FACTOR_3 * bits::bitCount(win_fields & tokens);
		}
		return score;
	}
}