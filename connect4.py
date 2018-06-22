from bits import to_flag

class Connect4:
	def __init__(self, width=7, height=6):
		self._width = width
		self._height = height
		self._player_tokens = [0, 0]
		self._current_player = 0
		self._full_board = to_flag(width * height) - 1
		
		xAxis = to_flag(width) - 1
		yAxis = self._full_board // xAxis
		self._win_shifts = [1, width - 1, width + 1, width]
		self._win_masks = [~yAxis, ~(xAxis | (yAxis << (width - 1))), ~(xAxis | yAxis), ~xAxis]
		for i in range(4):
			self._win_masks[i] &= self._win_masks[i] << self._win_shifts[i]
	
	def opponent(self):
		return self._current_player ^ 1
	
	def current_player(self):
		return self._current_player
		
	def occupied(self):
		return self._player_tokens[0] | self._player_tokens[1]
		
	def token_move_from_column(self, x):
		token = to_flag(x)
		tokens = self.occupied()
		while token & tokens:
			token <<= self._width
		return token
		
	def token_moves(self):
		tokens = self.occupied()
		return ~(tokens | ~tokens << self._width) & self._full_board
	
	def token_move(self, token):
		self._player_tokens[self._current_player] ^= token
		self._current_player = self.opponent()
	
	def token_undo(self, token):
		self._current_player = self.opponent()
		self._player_tokens[self._current_player] ^= token
		
	def is_board_full(self):
		return self.occupied() == self._full_board
		
	def opponent_won(self):
		tokens = self._player_tokens[self.opponent()]
		for i in range(4):
			win = tokens
			win &= win << (2 * self._win_shifts[i])
			win &= self._win_masks[i]
			win &= win << self._win_shifts[i]
			if(win):
				return True
		return False
		
	def is_game_over(self):
		return self.is_board_full() or self.opponent_won()
		
	def as_string(self):
		string = ""
		for y in reversed(range(self._height)):
			for x in range(self._width):
				token = to_flag(x + y * self._width)
				if(self._player_tokens[0] & token):
					string += "[X]"
				elif(self._player_tokens[1] & token):
					string += "[O]"
				else:
					string += "[ ]"
			string += "\n"
		return string
