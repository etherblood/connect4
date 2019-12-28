// connect4.cpp : This file contains the 'main' function. Program execution begins and ends there.
//

#include <iostream>
#include <chrono>
#include <ctime> 
#include "TokenSolver.h"
#include "TokenUtil.h"

int main()
{
	TokenSolver solver = TokenSolver();
	auto start = std::chrono::system_clock::now();
	std::cout << "Solution: " << solver.solve(0, 0) << "\n";
    auto end = std::chrono::system_clock::now();

    std::chrono::duration<double> elapsed_seconds = end - start;
    std::time_t end_time = std::chrono::system_clock::to_time_t(end);

    std::cout << "finished computation, elapsed time: " << elapsed_seconds.count() << "s\n";
    std::cout << TokenUtil::toString(0) << "\n";
}
