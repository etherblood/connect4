from connect4 import Connect4
from alphabeta import AlphaBetaBot
from transpositions import TranspositionTable

state = Connect4()
print(state.as_string())
while not state.is_game_over():
	try:
		x = int(input('Select move column:'))
	except ValueError:
		print("Not a number")
	else:
		if(x < 0 or state._width <= x):
			print("Invalid column")
		else:
			token_move = state.token_move_from_column(x)
			state.token_move(token_move)
			print(state.as_string())
			if not state.is_game_over():
				print("Computing...")
				state.token_move(AlphaBetaBot(table=TranspositionTable()).search(state))
			print(state.as_string())
print("Game over")
