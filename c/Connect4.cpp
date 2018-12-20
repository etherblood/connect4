#include "stdafx.h"
#include "Connect4.h"
#include "bits.h"


Connect4::Connect4(int width = 7, int height = 6)
{
	if(width * (height + 1) > 8 * sizeof(std::uint64_t))
	{
		throw std::invalid_argument("Given dimensions dont fit into std::uint64_t.");
	}
	this->width = width;
	this->height = height;
	this->currentPlayer = 0;
	this->fullBoard = bits::toFlag(width * height) - 1;
	this->xAxis = bits::toFlag(width) - 1;
	this->yAxis = this->fullBoard / this->xAxis;
	this->playerTokens[0] = 0;
	this->playerTokens[1] = 0;
	this->winShifts[0] = 1;
	this->winShifts[1] = width - 1;
	this->winShifts[2] = width + 1;
	this->winShifts[3] = width;
	this->winMasks[0] = ~this->yAxis;
	this->winMasks[1] = ~(this->xAxis | (this->yAxis << (width - 1)));
	this->winMasks[2] = ~(this->xAxis | this->yAxis);
	this->winMasks[3] = ~this->xAxis;
	for (int i = 0; i < 4; i++)
	{
		this->winMasks[i] &= this->winMasks[i] << this->winShifts[i];
	}
}

std::uint32_t Connect4::opponent() 
{
	return this->currentPlayer ^ 1;
}

std::uint32_t Connect4::activePlayer() 
{
	return this->currentPlayer;
}

std::uint64_t Connect4::occupied()
{
	return this->playerTokens[0] | this->playerTokens[1];
}

std::uint64_t Connect4::tokenMoveFromColumn(std::uint32_t x)
{
	std::uint64_t token = bits::toFlag(x);
	std::uint64_t tokens = this->occupied();
	while (token & tokens)
	{
		token <<= this->width;
	}
	return token & this->fullBoard;
}

std::uint64_t Connect4::tokenMoves()
{
	std::uint64_t tokens = this->occupied();
	return ~(tokens | ~tokens << this->width) & this->fullBoard;
}

void Connect4::tokenMove(std::uint64_t token)
{
	this->playerTokens[this->currentPlayer] ^= token;
	this->currentPlayer = this->opponent();
}

void Connect4::tokenUndo(std::uint64_t token)
{
	this->currentPlayer = this->opponent();
	this->playerTokens[this->currentPlayer] ^= token;
}

bool Connect4::isBoardFull()
{
	return this->occupied() == this->fullBoard;
}

bool Connect4::opponentWon()
{
	std::uint64_t tokens = this->playerTokens[this->opponent()];
	for (int i = 0; i < 4; i++)
	{
		std::uint64_t win = tokens;
		win &= win << (2 * this->winShifts[i]);
		win &= this->winMasks[i];
		win &= win << this->winShifts[i];
		if(win)
		{
			return true;
		}
	}
	return false;
}


bool Connect4::isGameOver()
{
	return this->isBoardFull() || this->opponentWon();
}


std::uint64_t Connect4::id()
{
	return ((this->occupied() << this->width) | this->xAxis) ^ this->playerTokens[0];
}

std::string Connect4::asString()
{
	std::string string = "";
	for (std::uint32_t y = this->height - 1; y < height; y--)
	{
		for (std::uint32_t x = 0; x < this->width; x++)
		{

			std::uint64_t token = bits::toFlag(x + y * this->width);
			if(this->playerTokens[0] & token){
				string += "[X]";
			}
			else if(this->playerTokens[1] & token)
			{
				string += "[O]";
			}
			else
			{
				string += "[ ]";
			}
		}
		if (y)
		{
			string += "\n";
		}
	}
	return string;

}

