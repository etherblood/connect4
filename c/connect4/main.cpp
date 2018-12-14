// connect4.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <iostream>
#include <string>
#include <stdexcept>
#include "Connect4.h"
#include "AlphaBetaBot.h"
#include "TranspositionTable.h"


int _tmain(int argc, _TCHAR* argv[])
{
	AlphabetaBot bot = AlphabetaBot(9, true, new TranspositionTable());
	Connect4 state = Connect4();
	while(!state.isGameOver()) {
		state.tokenMove(bot.search(&state));
		std::cout << state.asString() << std::endl;
	}
	return 0;
}