def toFlag(index):
	return 1 << index
	
class Connect4:
	
	def __init__(self, width=7, height=6):
		self.width = width
		self.height = height
		self.playerTokens = [0, 0]
		self.currentPlayer = 0
		
		xAxis = toFlag(width) - 1
		yAxis = 0
		for y in range(height):
			yAxis |= toFlag(y * width)
		self.fullBoard = xAxis * yAxis
		self.winShifts = [1, width - 1, width + 1, width]
		self.winMasks = [~yAxis, ~(xAxis | (yAxis << (width - 1))), ~(xAxis | yAxis), ~xAxis]
		for i in range(4):
			self.winMasks[i] &= self.winMasks[i] << self.winShifts[i]
	
	def opponent(self):
		return self.currentPlayer ^ 1
		
	def occupied(self):
		return self.playerTokens[0] | self.playerTokens[1]
		
	def columnToTokenMove(self, x):
		token = toFlag(x)
		tokens = self.occupied()
		while token & tokens:
			token <<= self.width
		return token
		
	def tokenMoves(self):
		tokens = self.occupied()
		return ~(tokens | ~tokens << self.width) & self.fullBoard
	
	def tokenMove(self, token):
		self.playerTokens[self.currentPlayer] ^= token
		self.currentPlayer = self.opponent()
	
	def tokenUndo(self, token):
		self.currentPlayer = self.opponent()
		self.playerTokens[self.currentPlayer] ^= token
		
	def isBoardFull(self):
		return self.occupied() == self.fullBoard
		
	def opponentWon(self):
		tokens = self.playerTokens[self.opponent()]
		for i in range(4):
			win = tokens
			win &= win << (2 * self.winShifts[i])
			win &= self.winMasks[i]
			win &= win << self.winShifts[i]
			if(win):
				return True
		return False
		
	def isGameOver(self):
		return self.isBoardFull() or self.opponentWon()
		
	def asString(self):
		string = ""
		for y in reversed(range(self.height)):
			for x in range(self.width):
				token = toFlag(x + y * self.width)
				if(self.playerTokens[0] & token):
					string += "[X]"
				elif(self.playerTokens[1] & token):
					string += "[O]"
				else:
					string += "[ ]"
			string += "\n"
		return string
		

state = Connect4()
print(state.asString())
while not state.isGameOver():
	try:
		x = int(input('Select move column:'))
	except ValueError:
		print("Not a number")
	else:
		if(x < 0 or state.width <= x):
			print("Invalid column")
		else:
			tokenMove = state.columnToTokenMove(x)
			state.tokenMove(tokenMove)
			print(state.asString())
print("Game over")