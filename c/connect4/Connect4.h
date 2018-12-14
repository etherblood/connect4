#pragma once

#include <string>
#include <cstdint>

class Connect4
{
public:
	Connect4(int width=7, int height=6);
	~Connect4(void);
	std::uint32_t opponent();
	std::uint32_t activePlayer();
	std::uint64_t occupied();
	std::uint64_t tokenMoveFromColumn(std::uint32_t x);
	std::uint64_t tokenMoves();
	std::uint64_t id();
	void tokenMove(std::uint64_t token);
	void tokenUndo(std::uint64_t token);
	bool isBoardFull();
	bool isGameOver();
	bool opponentWon();
	std::string asString();
	
	std::uint64_t winShifts [4];
	std::uint64_t winMasks [4];
	std::uint64_t playerTokens [2];
	std::uint32_t width, height, currentPlayer;
	std::uint64_t fullBoard, xAxis, yAxis;
private:

};


