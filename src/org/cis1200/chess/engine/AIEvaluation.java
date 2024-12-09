package org.cis1200.chess.engine;

public class AIEvaluation {
    public Move move;
    public int score;
    public AIEvaluation(Move move, int score) {
        this.move = move;
        this.score = score;
    }
}
