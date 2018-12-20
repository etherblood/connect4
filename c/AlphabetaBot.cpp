#include "stdafx.h"
#include "AlphabetaBot.h"
#include "bits.h"
#include "evaluation.h"
#include <iostream>


AlphabetaBot::AlphabetaBot(std::uint32_t depth, bool pvsEnabled, TranspositionTable* table, bool verbose)
{
	this->depth = depth;
	this->table = table;
	this->pvsEnabled = pvsEnabled;
	this->verbose = verbose;
}


AlphabetaBot::~AlphabetaBot(void)
{
}

std::uint64_t AlphabetaBot::search(Connect4* state)
{
	this->nodes = 1;
	std::uint64_t bestMove;
	std::int32_t alpha = evaluation::LOSS_SCORE - this->depth;
	std::int32_t beta = -alpha;
	bool foundPV = false;
	std::uint64_t moves = state->tokenMoves();
	while(moves)
	{
		std::uint64_t move = bits::highestBit(moves);
		moves ^= move;
		state->tokenMove(move);
		std::int32_t score;
		if (this->pvsEnabled && foundPV)
		{
			score = -this->alphabeta(state, this->depth - 1, -alpha - 1, -alpha);
			if (alpha < score)
			{
				score = -this->alphabeta(state, this->depth - 1, -beta, -alpha);
			}
		}
		else
		{
			score = -this->alphabeta(state, this->depth - 1, -beta, -alpha);
		}
		state->tokenUndo(move);
		if(score > alpha)
		{
			alpha = score;
			bestMove = move;
			foundPV = true;
		}
	}
	if (this->verbose)
	{
		std::cout << "nodes searched: " << this->nodes << std::endl;
		if (this->table)
		{
			this->table->printStats();
		}
	}
	return bestMove;
}

std::int32_t AlphabetaBot::alphabeta(Connect4* state, std::uint32_t depth, std::int32_t alpha, std::int32_t beta)
{
	this->nodes += 1;
	if (state->opponentWon())
	{
		return evaluation::LOSS_SCORE - depth;
	}
	if (state->isBoardFull())
	{
		return 0;
	}
	if (depth <= 0)
	{
		return evaluation::evaluate(state);
	}

	if (this->table)
	{
		TranspositionEntry entry;
		if (this->table->load(state->id(), &entry) && entry.depth >= depth)
		{
			switch (entry.type)
			{
			case UPPER_BOUND:
				beta = entry.score;
				if (alpha >= beta)
				{
					return alpha;
				}
				break;
			case LOWER_BOUND:
				alpha = entry.score;
				if (alpha >= beta)
				{
					return beta;
				}
				break;
			case EXACT:
				return entry.score;
			}
		}
	}

	ScoreType type = UPPER_BOUND;
	bool foundPV = false;
	std::uint64_t moves = state->tokenMoves();
	while(moves)
	{
		std::uint64_t move = bits::highestBit(moves);
		moves ^= move;
		state->tokenMove(move);
		std::int32_t score;
		if (this->pvsEnabled && foundPV)
		{
			score = -this->alphabeta(state, depth - 1, -alpha - 1, -alpha);
			if (alpha < score)
			{
				score = -this->alphabeta(state, depth - 1, -beta, -alpha);
			}
		}
		else
		{
			score = -this->alphabeta(state, depth - 1, -beta, -alpha);
		}
		state->tokenUndo(move);
		if (score > alpha)
		{
			if (score >= beta)
			{
				alpha = beta;
				type = LOWER_BOUND;
				break;
			}
			alpha = score;
			foundPV = true;
			type = EXACT;
		}
	}

	if (this->table)
	{
		TranspositionEntry entry = TranspositionEntry();
		entry.id = state->id();
		entry.type = type;
		entry.score = alpha;
		entry.depth = depth;
		this->table->store(entry);
	}
	return alpha;
}