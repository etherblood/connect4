from connect4 import Connect4
from connect4 import to_flag
from evaluation import evaluate
from evaluation import LOSS_SCORE

def lowest_bit(i):
	return i & -i

def highest_bit_index(i):
	return i.bit_length() - 1
	
def highest_bit(i):
	return to_flag(highest_bit_index(i))

class AlphaBetaBot:
	def __init__(self, depth=7):
		self._depth = depth
		
	def search(self, state):
		alpha = LOSS_SCORE - self._depth
		beta = -alpha
		moves = state.token_moves()
		while moves:
			move = highest_bit(moves)
			moves ^= move
			state.token_move(move)
			score = -self.alphabeta(state, self._depth - 1, -beta, -alpha)
			state.token_undo(move)
			if score > alpha:
				alpha = score
				best_move = move
		return best_move
		
	def alphabeta(self, state, depth, alpha, beta):
		if state.opponent_won():
			return LOSS_SCORE - depth
		if state.is_board_full():
			return 0
		if depth <= 0:
			return evaluate(state)
		moves = state.token_moves()
		while moves:
			move = highest_bit(moves)
			moves ^= move
			state.token_move(move)
			score = -self.alphabeta(state, depth - 1, -beta, -alpha)
			state.token_undo(move)
			if score > alpha:
				if score >= beta:
					alpha = beta
					break
				alpha = score
		return alpha
	