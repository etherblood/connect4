package com.etherblood.connect4.eval;

import com.etherblood.connect4.Connect4StateImpl;
import static com.etherblood.connect4.TokenUtil.squish;
import static com.etherblood.connect4.TokenUtil.stretch;

public class SimpleEvaluation implements Evaluation {

    private static final int LOSS = -10_000;
    private static final int SIDE_TO_MOVE = 10;
    private static final int FACTOR_3 = 18;

    private final Connect4StateImpl state;

    public SimpleEvaluation(Connect4StateImpl state) {
        this.state = state;
    }

    @Override
    public int evaluate() {
        int score = 0;
        score += score(state.right);
        score += score(state.rightDown);
        score += score(state.rightUp);
        score += score(state.up);
        if (state.player1Active) {
            score = -score;
        }
        score += SIDE_TO_MOVE;
        return score;
    }

    private int score(int direction) {
        int score = 0;
        long tokens0 = state.player0Tokens;
        long tokens1 = state.player1Tokens;
        long squishedCandidates = squish(state.fullBoard & ~tokens1, direction);
        long candidates = stretch(squishedCandidates, direction);
        long tokens = tokens0;
        tokens &= tokens << direction;
        score += Long.bitCount(candidates & tokens);
        tokens &= tokens << direction;
        score += FACTOR_3 * Long.bitCount(candidates & tokens);

        squishedCandidates = squish(state.fullBoard & ~tokens0, direction);
        candidates = stretch(squishedCandidates, direction);
        tokens = tokens1;
        tokens &= tokens << direction;
        score -= Long.bitCount(candidates & tokens);
        tokens &= tokens << direction;
        score -= FACTOR_3 * Long.bitCount(candidates & tokens);
        return score;
    }

    @Override
    public int lossScore() {
        return LOSS;
    }

}
