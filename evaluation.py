from connect4 import Connect4

SIDE_TO_MOVE = 10
FACTOR_3 = 18

LOSS_SCORE = -10000

def bit_count(i):
    i = i - ((i >> 1) & 0x55555555)
    i = (i & 0x33333333) + ((i >> 2) & 0x33333333)
    return (((i + (i >> 4) & 0xF0F0F0F) * 0x1010101) & 0xffffffff) >> 24
    
def shifts4(state, mask, direction):
	mask &= mask << (2 * state._win_shifts[direction])
	mask &= state._win_masks[direction]
	mask &= mask << state._win_shifts[direction]
	return mask
    
def strech4(state, mask, direction):
	mask |= mask >> (2 * state._win_shifts[direction])
	mask |= mask >> state._win_shifts[direction]
	return mask

def evaluate(state):
	current_player = state.current_player()
	opponent = state.opponent()
	own_tokens = state._player_tokens[current_player]
	opponent_tokens = state._player_tokens[opponent]

	score = SIDE_TO_MOVE
	for i in range(4):
		win_positions = shifts4(state, state._full_board & ~opponent_tokens, i)
		win_fields = strech4(state, win_positions, i)
		tokens = own_tokens
		tokens &= tokens << state._win_shifts[i]
		score += bit_count(win_fields & tokens)
		tokens &= tokens << state._win_shifts[i]
		score += FACTOR_3 * bit_count(win_fields & tokens)
            
		win_positions = shifts4(state, state._full_board & ~own_tokens, i)
		win_fields = strech4(state, win_positions, i)
		tokens = opponent_tokens
		tokens &= tokens << state._win_shifts[i]
		score -= bit_count(win_fields & tokens)
		tokens &= tokens << state._win_shifts[i]
		score -= FACTOR_3 * bit_count(win_fields & tokens)
	return score
