from connect4 import Connect4
from alphabeta import AlphaBetaBot
from transpositions import TranspositionTable

def play():
	state = Connect4()
	table = TranspositionTable()
	bot = AlphaBetaBot(table=table)
	print(state.as_string())
	while not state.is_game_over():
		try:
			x = int(input("Select move column:"))
		except ValueError:
			print("Not a number")
		else:
			token_move = state.token_move_from_column(x)
			if not token_move or x < 0 or state._width <= x:
				print("Invalid move")
			else:
				state.token_move(token_move)
				print(state.as_string())
				if not state.is_game_over():
					print("Computing...")
					state.token_move(bot.search(state))
				print(state.as_string())
	print("Game over")

if __name__ == "__main__":
	play()
