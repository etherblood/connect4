from connect4 import Connect4
from evaluation import evaluate
from evaluation import LOSS_SCORE

def lowest_bit(i):
	return i & -i

class AlphaBetaBot:
	def __init__(self, depth=7):
		self._depth = depth
		
	def search(self, state):
		alpha = LOSS_SCORE - self._depth - 1
		beta = -alpha
		moves = state.token_moves()
		while moves:
			move = lowest_bit(moves)
			moves ^= move
			state.token_move(move)
			score = -self.alphabeta(state, self._depth - 1, -beta, -alpha)
			state.token_undo(move)
			if score > alpha:
				alpha = score
				best_move = move
		return best_move
		
	def alphabeta(self, state, depth, alpha, beta):
		if depth <= 0:
			return evaluate(state)
		if state.is_game_over():
			if state.opponent_won():
				return LOSS_SCORE - depth
			else:
				return 0
		moves = state.token_moves()
		while moves:
			move = lowest_bit(moves)
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
	