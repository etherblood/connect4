from connect4 import Connect4
from bits import sparse_bit_count

SIDE_TO_MOVE = 10
FACTOR_3 = 18

LOSS_SCORE = -10000
    
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
		score += sparse_bit_count(win_fields & tokens)
		tokens &= tokens << state._win_shifts[i]
		score += FACTOR_3 * sparse_bit_count(win_fields & tokens)
            
		win_positions = shifts4(state, state._full_board & ~own_tokens, i)
		win_fields = strech4(state, win_positions, i)
		tokens = opponent_tokens
		tokens &= tokens << state._win_shifts[i]
		score -= sparse_bit_count(win_fields & tokens)
		tokens &= tokens << state._win_shifts[i]
		score -= FACTOR_3 * sparse_bit_count(win_fields & tokens)
	return score
