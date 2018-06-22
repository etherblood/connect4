from connect4 import Connect4
from bits import highest_bit
from evaluation import evaluate
from evaluation import LOSS_SCORE

class AlphaBetaBot:
	def __init__(self, depth=7, pvs_enabled=True):
		self._depth = depth
		self._pvs_enabled = pvs_enabled
		
	def search(self, state):
		alpha = LOSS_SCORE - self._depth
		beta = -alpha
		foundPV = False
		moves = state.token_moves()
		while moves:
			move = highest_bit(moves)
			moves ^= move
			state.token_move(move)
			if self._pvs_enabled and foundPV:
				score = -self.alphabeta(state, self._depth - 1, -alpha - 1, -alpha)
				if alpha < score:
					score = -self.alphabeta(state, self._depth - 1, -beta, -alpha)
			else:
				score = -self.alphabeta(state, self._depth - 1, -beta, -alpha)
			state.token_undo(move)
			if score > alpha:
				alpha = score
				best_move = move
				foundPV = True
		return best_move
		
	def alphabeta(self, state, depth, alpha, beta):
		if state.opponent_won():
			return LOSS_SCORE - depth
		if state.is_board_full():
			return 0
		if depth <= 0:
			return evaluate(state)
		foundPV = False
		moves = state.token_moves()
		while moves:
			move = highest_bit(moves)
			moves ^= move
			state.token_move(move)
			if self._pvs_enabled and foundPV:
				score = -self.alphabeta(state, depth - 1, -alpha - 1, -alpha)
				if alpha < score:
					score = -self.alphabeta(state, depth - 1, -beta, -alpha)
			else:
				score = -self.alphabeta(state, depth - 1, -beta, -alpha)
			state.token_undo(move)
			if score > alpha:
				if score >= beta:
					alpha = beta
					break
				alpha = score
				foundPV = True
		return alpha
	