package org.cis1200.chess.engine;
import org.cis1200.chess.engine.CastleState;

public class Move {
    public int source;
    public int target;
    public int piece;
    public boolean isCaptureMove;
    public int pieceCaptured;
    public boolean isPromotionMove;
    public int promotionPiece;
    public boolean isCastleMove;
    public boolean rightCastleDirection;
    public CastleState castleState;
    public boolean isEnPassantMove;
    public Move(int source, int target, int piece,
                boolean isCaptureMove, int pieceCaptured, boolean isPromotionMove,
                int promotionPiece, boolean isCastleMove,
                boolean rightCastleDirection, CastleState castleState, boolean isEnPassantMove) {
        this.source = source;
        this.target = target;
        this.piece = piece;
        this.isCaptureMove = isCaptureMove;
        this.pieceCaptured = pieceCaptured;
        this.isPromotionMove = isPromotionMove;
        this.promotionPiece = promotionPiece;
        this.isCastleMove = isCastleMove;
        this.rightCastleDirection = rightCastleDirection;
        this.castleState = castleState;
        this.isEnPassantMove = isEnPassantMove;
    }

    @Override
    public String toString() {
        String returnString = "{";
        returnString += source + ", ";
        returnString += target + ", ";
        returnString += piece + ", ";
        returnString += isCaptureMove + ", ";
        returnString += pieceCaptured + ", ";
        returnString += isPromotionMove + ", ";
        returnString += isCastleMove + ", ";
        returnString += rightCastleDirection + ", ";
        returnString += castleState + ", ";
        returnString += isEnPassantMove + "}";
        return returnString;
    }

}
