package com.etherblood.connect4.eval;

/**
 *
 * @author Philipp
 */
public interface Evaluation {

    int evaluate();

    int lossScore();

    default int drawScore() {
        return 0;
    }
}
