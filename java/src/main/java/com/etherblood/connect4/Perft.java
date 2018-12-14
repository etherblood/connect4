package com.etherblood.connect4;

/**
 *
 * @author Philipp
 */
public class Perft {

    private final Connect4State state;

    public Perft(Connect4State state) {
        this.state = state;
    }
    
    public long perft(int depth) {
        if(depth < 0) {
            throw new IllegalArgumentException();
        }
        if(depth == 0) {
            return 1;
        }
        return positiveDepthPerft(depth);
    }
    
    private long positiveDepthPerft(int depth) {
        if(state.isGameOver()) {
            return 0;
        }
        if(depth == 1) {
            return Long.bitCount(state.tokenMoves());
        }
        long sum = 0;
        long moves = state.tokenMoves();
        while(moves != 0) {
            long move = Long.lowestOneBit(moves);
            state.tokenMove(move);
            sum += positiveDepthPerft(depth - 1);
            state.tokenUndo(move);
            moves ^= move;
        }
        return sum;
    }
}
