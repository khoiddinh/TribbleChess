package org.cis1200.chess.engine;

public class AIEvaluation {
    private Move move;
    private int score;
    public AIEvaluation(Move move, int score) {
        this.move = move;
        this.score = score;
    }
    public Move move() {
        return move;
    }
    public int score() {
        return score;
    }


}
