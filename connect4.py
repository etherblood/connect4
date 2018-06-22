def to_flag(index):
	return 1 << index
	
class Connect4:
	def __init__(self, width=7, height=6):
		self.__width = width
		self.__height = height
		self.__player_tokens = [0, 0]
		self.__current_player = 0
		
		xAxis = to_flag(width) - 1
		yAxis = 0
		for y in range(height):
			yAxis |= to_flag(y * width)
		self.__full_board = xAxis * yAxis
		self.__win_shifts = [1, width - 1, width + 1, width]
		self.__win_masks = [~yAxis, ~(xAxis | (yAxis << (width - 1))), ~(xAxis | yAxis), ~xAxis]
		for i in range(4):
			self.__win_masks[i] &= self.__win_masks[i] << self.__win_shifts[i]
	
	def opponent(self):
		return self.__current_player ^ 1
	
	def current_player(self):
		return self.__current_player
		
	def occupied(self):
		return self.__player_tokens[0] | self.__player_tokens[1]
		
	def convert_column_to_token_move(self, x):
		token = to_flag(x)
		tokens = self.occupied()
		while token & tokens:
			token <<= self.__width
		return token
		
	def token_moves(self):
		tokens = self.occupied()
		return ~(tokens | ~tokens << self.__width) & self.__full_board
	
	def token_move(self, token):
		self.__player_tokens[self.__current_player] ^= token
		self.__current_player = self.opponent()
	
	def token_undo(self, token):
		self.__current_player = self.opponent()
		self.__player_tokens[self.__current_player] ^= token
		
	def is_board_full(self):
		return self.occupied() == self.__full_board
		
	def opponent_won(self):
		tokens = self.__player_tokens[self.opponent()]
		for i in range(4):
			win = tokens
			win &= win << (2 * self.__win_shifts[i])
			win &= self.__win_masks[i]
			win &= win << self.__win_shifts[i]
			if(win):
				return True
		return False
		
	def is_game_over(self):
		return self.is_board_full() or self.opponent_won()
		
	def as_string(self):
		string = ""
		for y in reversed(range(self.__height)):
			for x in range(self.__width):
				token = to_flag(x + y * self.__width)
				if(self.__player_tokens[0] & token):
					string += "[X]"
				elif(self.__player_tokens[1] & token):
					string += "[O]"
				else:
					string += "[ ]"
			string += "\n"
		return string
		

state = Connect4()
while not state.is_game_over():
	print(state.as_string())
	try:
		x = int(input('Select move column:'))
	except ValueError:
		print("Not a number")
	else:
		if(x < 0 or state.width <= x):
			print("Invalid column")
		else:
			token_move = state.convert_column_to_token_move(x)
			state.token_move(token_move)
print(state.as_string())
print("Game over")