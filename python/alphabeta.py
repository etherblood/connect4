from connect4 import Connect4
from bits import highest_bit
from evaluation import evaluate, LOSS_SCORE
from transpositions import UPPER_BOUND, LOWER_BOUND, EXACT

class AlphaBetaBot:
	def __init__(self, depth=9, pvs_enabled=True, table=None, verbose=True):
		self._depth = depth
		self._pvs_enabled = pvs_enabled
		self._table = table
		self._verbose = verbose
		
	def search(self, state):
		self._nodes = 1
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
		if self._verbose:
			print("nodes searched: " + str(self._nodes))
			if self._table:
				self._table.print_stats()
		return best_move
		
	def alphabeta(self, state, depth, alpha, beta):
		self._nodes += 1
		if state.opponent_won():
			return LOSS_SCORE - depth
		if state.is_board_full():
			return 0
		if depth <= 0:
			return evaluate(state)
		
		if self._table:
			entry = self._table.load(state.id())
			if entry and entry.depth >= depth:
				if entry.type == UPPER_BOUND:
					beta = entry.score
					if alpha >= beta:
						return alpha
				if entry.type == LOWER_BOUND:
					alpha = entry.score
					if alpha >= beta:
						return beta
				if entry.type == EXACT:
					return entry.score
		
		type = UPPER_BOUND
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
					type = LOWER_BOUND
					break
				alpha = score
				foundPV = True
				type = EXACT
		
		if self._table:
			self._table.store_raw(state.id(), type, alpha, depth)
		return alpha
	