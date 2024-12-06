package org.cis1200.chess.engine;
import org.cis1200.chess.engine.ChessBoard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import static org.cis1200.chess.engine.MoveGenerationPrecompute.*;

public class ChessEngine {

    private static HashMap<Integer, Integer> PIECE_TO_SCORE;

    private static final int MAX = 1000;
    private static final int MIN = -1000;
    private static final int MAX_SEARCH_DEPTH = 5;

    public int nodesSearched = 0;


    private static final int[] WHITE_PAWN_POS_TABLE =
            {0,  0,  0,  0,  0,  0,  0,  0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5,  5, 10, 25, 25, 10,  5,  5,
            0,  0,  0, 20, 20,  0,  0,  0,
            5, -5,-10,  0,  0,-10, -5,  5,
            5, 10, 10,-20,-20, 10, 10,  5,
            0,  0,  0,  0,  0,  0,  0,  0};

    private static final int[] WHITE_KNIGHT_POS_TABLE =
            {-50,-40,-30,-30,-30,-30,-40,-50,
            -40,-20,  0,  0,  0,  0,-20,-40,
            -30,  0, 10, 15, 15, 10,  0,-30,
            -30,  5, 15, 20, 20, 15,  5,-30,
            -30,  0, 15, 20, 20, 15,  0,-30,
            -30,  5, 10, 15, 15, 10,  5,-30,
            -40,-20,  0,  5,  5,  0,-20,-40,
            -50,-40,-30,-30,-30,-30,-40,-50};

    private static final int[] WHITE_BISHOP_POS_TABLE =
            {-20,-10,-10,-10,-10,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5, 10, 10,  5,  0,-10,
            -10,  5,  5, 10, 10,  5,  5,-10,
            -10,  0, 10, 10, 10, 10,  0,-10,
            -10, 10, 10, 10, 10, 10, 10,-10,
            -10,  5,  0,  0,  0,  0,  5,-10,
            -20,-10,-10,-10,-10,-10,-10,-20,};

    private static final int[] WHITE_ROOK_POS_TABLE =
            {0,  0,  0,  0,  0,  0,  0,  0,
            5, 10, 10, 10, 10, 10, 10,  5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            0,  0,  0,  5,  5,  0,  0,  0};

    private static final int[] WHITE_QUEEN_POS_TABLE =
            {-20,-10,-10, -5, -5,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5,  5,  5,  5,  0,-10,
            -5,  0,  5,  5,  5,  5,  0, -5,
            0,  0,  5,  5,  5,  5,  0, -5,
            -10,  5,  5,  5,  5,  5,  0,-10,
            -10,  0,  5,  0,  0,  0,  0,-10,
            -20,-10,-10, -5, -5,-10,-10,-20};

    private static final int[] WHITE_KING_POS_TABLE =
            {-30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -20,-30,-30,-40,-40,-30,-30,-20,
            -10,-20,-20,-20,-20,-20,-20,-10,
            20, 20,  0,  0,  0,  0, 20, 20,
            20, 30, 10,  0,  0, 10, 30, 20};

    private static final int[] WHITE_KING_POS_ENDGAME_TABLE =
            {-50,-40,-30,-20,-20,-30,-40,-50,
            -30,-20,-10,  0,  0,-10,-20,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-30,  0,  0,  0,  0,-30,-30,
            -50,-30,-30,-30,-30,-30,-30,-50};

    private static final int[] BLACK_PAWN_POS_TABLE =
            {0,  0,  0,  0,  0,  0,  0,  0,
                    5, 10, 10,-20,-20, 10, 10,  5,
                    5, -5,-10,  0,  0,-10, -5,  5,
                    0,  0,  0, 20, 20,  0,  0,  0,
                    5,  5, 10, 25, 25, 10,  5,  5,
                    10, 10, 20, 30, 30, 20, 10, 10,
                    50, 50, 50, 50, 50, 50, 50, 50,
                    0,  0,  0,  0,  0,  0,  0,  0};

    private static final int[] BLACK_KNIGHT_POS_TABLE =
            {-50,-40,-30,-30,-30,-30,-40,-50,
                    -40,-20,  0,  5,  5,  0,-20,-40,
                    -30,  5, 10, 15, 15, 10,  5,-30,
                    -30,  0, 15, 20, 20, 15,  0,-30,
                    -30,  5, 15, 20, 20, 15,  5,-30,
                    -30,  0, 10, 15, 15, 10,  0,-30,
                    -40,-20,  0,  0,  0,  0,-20,-40,
                    -50,-40,-30,-30,-30,-30,-40,-50};

    private static final int[] BLACK_BISHOP_POS_TABLE =
            {-20,-10,-10,-10,-10,-10,-10,-20,
                    -10,  5,  0,  0,  0,  0,  5,-10,
                    -10, 10, 10, 10, 10, 10, 10,-10,
                    -10,  0, 10, 10, 10, 10,  0,-10,
                    -10,  5,  5, 10, 10,  5,  5,-10,
                    -10,  0,  5, 10, 10,  5,  0,-10,
                    -10,  0,  0,  0,  0,  0,  0,-10,
                    -20,-10,-10,-10,-10,-10,-10,-20};

    private static final int[] BLACK_ROOK_POS_TABLE =
            {0,  0,  0,  5,  5,  0,  0,  0,
                    -5,  0,  0,  0,  0,  0,  0, -5,
                    -5,  0,  0,  0,  0,  0,  0, -5,
                    -5,  0,  0,  0,  0,  0,  0, -5,
                    -5,  0,  0,  0,  0,  0,  0, -5,
                    -5,  0,  0,  0,  0,  0,  0, -5,
                    5, 10, 10, 10, 10, 10, 10,  5,
                    0,  0,  0,  0,  0,  0,  0,  0};

    private static final int[] BLACK_QUEEN_POS_TABLE =
            {-20,-10,-10, -5, -5,-10,-10,-20,
                    -10,  0,  5,  0,  0,  0,  0,-10,
                    -10,  5,  5,  5,  5,  5,  0,-10,
                    0,  0,  5,  5,  5,  5,  0, -5,
                    -5,  0,  5,  5,  5,  5,  0, -5,
                    -10,  0,  5,  5,  5,  5,  0,-10,
                    -10,  0,  0,  0,  0,  0,  0,-10,
                    -20,-10,-10, -5, -5,-10,-10,-20};

    private static final int[] BLACK_KING_POS_TABLE =
            {20, 30, 10,  0,  0, 10, 30, 20,
                    20, 20,  0,  0,  0,  0, 20, 20,
                    -10,-20,-20,-20,-20,-20,-20,-10,
                    -20,-30,-30,-40,-40,-30,-30,-20,
                    -30,-40,-40,-50,-50,-40,-40,-30,
                    -30,-40,-40,-50,-50,-40,-40,-30,
                    -30,-40,-40,-50,-50,-40,-40,-30,
                    -30,-40,-40,-50,-50,-40,-40,-30};

    private static final int[] BLACK_KING_POS_ENDGAME_TABLE =
            {-50,-30,-30,-30,-30,-30,-30,-50,
                    -30,-30,  0,  0,  0,  0,-30,-30,
                    -30,-10, 20, 30, 30, 20,-10,-30,
                    -30,-10, 30, 40, 40, 30,-10,-30,
                    -30,-10, 30, 40, 40, 30,-10,-30,
                    -30,-10, 20, 30, 30, 20,-10,-30,
                    -30,-20,-10,  0,  0,-10,-20,-30,
                    -50,-40,-30,-20,-20,-30,-40,-50};

    private static final int[][] WHITE_PIECE_POS_TABLE =
            {WHITE_KING_POS_TABLE,
            WHITE_QUEEN_POS_TABLE,
            WHITE_ROOK_POS_TABLE,
            WHITE_BISHOP_POS_TABLE,
            WHITE_KNIGHT_POS_TABLE,
            WHITE_PAWN_POS_TABLE};

    private static final int[][] BLACK_PIECE_POS_TABLE =
            {BLACK_KING_POS_TABLE,
                    BLACK_QUEEN_POS_TABLE,
                    BLACK_ROOK_POS_TABLE,
                    BLACK_BISHOP_POS_TABLE,
                    BLACK_KNIGHT_POS_TABLE,
                    BLACK_PAWN_POS_TABLE};

    public ChessEngine() {
        PIECE_TO_SCORE = new HashMap<>();
        PIECE_TO_SCORE.put(0, 0); // king = 0 points
        PIECE_TO_SCORE.put(1, 9); // queen = 9 points
        PIECE_TO_SCORE.put(2, 5); // rook == 5 points
        PIECE_TO_SCORE.put(3, 3); // bishop = 3 points
        PIECE_TO_SCORE.put(4, 3); // knight = 3 points
        PIECE_TO_SCORE.put(5, 1); // pawn = 1 point

    }

    private int scoreMove (int move) {
        int source = move & 0b111111;
        int target = (move & (0b111111 << 6)) >>> 6;
        int piece = (move & (0b111 << 12)) >>> 12;
        boolean capture = (move & (1 << 15)) >>> 15 == 1;
        int pieceCaptured = (move & (0b111 << 16)) >>> 16;
        boolean promotion = ((move & (1 << 19)) >>> 19) == 1;
        int promotionPiece = (move & (0b11 << 20)) >>> 20;
        boolean castleMove = ((move & (1 << 22)) >>> 22) == 1;
        int castleDirection = (move & (1 << 23)) >>> 23;
        int castleState = (move & (0b1111 << 24)) >>> 24; // previous state before move
        boolean enPassant = ((move & (1 << 28)) >>> 28) == 1;

        int score = 0;
        if (promotion) {
            score += 3 * promotionPiece;
        }
        if (capture) {
            score += 2 * (PIECE_TO_SCORE.get(pieceCaptured) - PIECE_TO_SCORE.get(piece));
        }
        if (castleMove) {
            score += castleDirection == 0 ? 10 : 8; // slightly prefer right castle
        }
        return score;

    }
    private int compareMove(Integer move1, Integer move2) {
        return scoreMove(move2) - scoreMove(move1);
    }

    // sort in place
    private void sortMoves(ArrayList<Integer> moves) {
        Collections.sort(moves, (move1, move2) -> compareMove(move1, move2));
    }
    // eval board: + nums good for white, - nums good for black
    private int evalBoard(ChessBoard board) {
        // white score
        int whiteScore = 0;
        int whitePieceScore = 0; // amount of pieces white has
        for (int piece = 1; piece < 6; piece++) { // don't consider kings (do later)
            long pieceBitBoard = board.whiteBitBoards[piece];
            int pieceScore = PIECE_TO_SCORE.get(piece);
            while (pieceBitBoard != 0) {
                int pos = board.getPosOfLeastSigBit(pieceBitBoard);
                whiteScore += WHITE_PIECE_POS_TABLE[piece][pos]; // add location bonus
                whitePieceScore += pieceScore; // add piece score bonus
                pieceBitBoard ^= startingBitBoards[pos]; // remove piece from consideration
            }
        }

        // black score
        int blackScore = 0;
        int blackPieceScore = 0; // amount of pieces white has
        for (int piece = 1; piece < 6; piece++) { // don't consider kings (do later)
            long pieceBitBoard = board.blackBitBoards[piece];
            int pieceScore = PIECE_TO_SCORE.get(piece);
            while (pieceBitBoard != 0) {
                int pos = board.getPosOfLeastSigBit(pieceBitBoard);
                blackScore += BLACK_PIECE_POS_TABLE[piece][pos]; // add location bonus
                blackPieceScore += pieceScore; // add piece score bonus
                pieceBitBoard ^= startingBitBoards[pos]; // remove piece from consideration
            }
        }
        if (whitePieceScore < 14 && blackPieceScore < 14) { // endgame (not many pieces)
            int whiteKingPos = board.getPosOfLeastSigBit(board.whiteBitBoards[0]);
            whiteScore += WHITE_KING_POS_ENDGAME_TABLE[whiteKingPos];
            int blackKingPos = board.getPosOfLeastSigBit(board.blackBitBoards[0]);
            blackScore += BLACK_KING_POS_ENDGAME_TABLE[blackKingPos];
        } else {
            int whiteKingPos = board.getPosOfLeastSigBit(board.whiteBitBoards[0]);
            whiteScore += WHITE_KING_POS_TABLE[whiteKingPos];
            int blackKingPos = board.getPosOfLeastSigBit(board.blackBitBoards[0]);
            blackScore += BLACK_KING_POS_TABLE[blackKingPos];
        }
        return (whiteScore + 3 * whitePieceScore) - (blackScore + 3 * blackPieceScore);
    }

    public int getBestMove(ChessBoard board) {
        int[] result = minimax(board, 0, board.isWhiteTurn(), MIN, MAX);
        return result[0];
    }
    // maximizing player = white
    // returns 2 element int array [move (int), score]
    private int[] minimax(ChessBoard board, int depth, boolean isMaximizingPlayer, int alpha, int beta) {
        nodesSearched += 1;
        ArrayList<Integer> moves = board.getLegalPossibleMoves();
        int gameState = board.checkWinner(moves);
        if (gameState == 1) { // white win
            return new int[]{-1, MAX};
        }
        else if (gameState == -1) { // black win
            return new int[]{-1, MIN};
        } else if (gameState == 2) { // draw
            return new int[]{-1, 0};
        } else if (depth == MAX_SEARCH_DEPTH) {
            return new int[]{-1, evalBoard(board)};
        }
        if (isMaximizingPlayer) { // white
            int bestScore = MIN;
            int bestMove = -1;
            for (int move : moves) {

                board.makeMove(move);
                int[] result = minimax(board, depth+1,
                        false, alpha, beta);
                int currScore = result[1];
                board.undoLastMove();
                if (currScore > bestScore) {
                    bestScore = currScore;
                    bestMove = move;
                }

                alpha = Math.max(bestScore, alpha);

                if (beta <= alpha) {
                    break;
                }
            }
            return new int[]{bestMove, bestScore};
        } else { // black
            int bestScore = MAX;
            int bestMove = -1;
            for (int move : moves) {

                board.makeMove(move);
                int[] result = minimax(board, depth+1,
                        false, alpha, beta);
                int currScore = result[1];
                board.undoLastMove();
                if (currScore < bestScore) {
                    bestScore = currScore;
                    bestMove = move;
                }

                beta = Math.min(bestScore, beta);

                if (beta <= alpha) {
                    break;
                }
            }
            return new int[]{bestMove, bestScore};
        }
    }

}
