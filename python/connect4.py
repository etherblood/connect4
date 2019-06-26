from bits import to_flag
from bits import to_mask

class Connect4:
	def __init__(self, width=7, height=6):
		self._width = width
		self._height = height
		self._buffered_height = height + 1
		self._player_tokens = [0, 0]
		self._current_player = 0
		self._y_axis = to_mask(height)
		self._x_axis = to_mask(width * self._buffered_height) // to_mask(self._buffered_height)
		self._full_board = self._x_axis * self._y_axis
		self._up = 1
		self._right = self._buffered_height
		self._right_up = self._right + self._up
		self._right_down = self._right - self._up
	
	def opponent(self):
		return self._current_player ^ 1
	
	def current_player(self):
		return self._current_player
		
	def occupied(self):
		return self._player_tokens[0] | self._player_tokens[1]
		
	def token_move_from_column(self, x):
		column_mask = self._y_axis << (x * self._buffered_height)
		return (self.occupied() + self._x_axis) & column_mask
		
	def token_moves(self):
		return (self.occupied() + self._x_axis) & self._full_board
	
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
		return self.squish(tokens, self._right_up) or self.squish(tokens, self._right_down) or self.squish(tokens, self._right) or self.squish(tokens, self._up)
	
	def squish(self, tokens, direction):
		tokens &= tokens << (2 * direction)
		tokens &= tokens << direction
		return tokens;
		
	def is_game_over(self):
		return self.is_board_full() or self.opponent_won()
	
	def id(self):
		return (self.occupied() + self._x_axis) ^ self._player_tokens[0]
		
	def as_string(self):
		string = ""
		for y in reversed(range(self._height)):
			for x in range(self._width):
				token = to_flag(x * self._buffered_height + y)
				if(self._player_tokens[0] & token):
					string += "[X]"
				elif(self._player_tokens[1] & token):
					string += "[O]"
				else:
					string += "[ ]"
			if y:
				string += "\n"
		return string
