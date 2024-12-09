package org.cis1200.chess.engine;
import org.cis1200.chess.engine.CastleState;

public class Move {
    private int source;
    private int target;
    private int piece;
    private boolean isCaptureMove;
    private int pieceCaptured;
    private boolean isPromotionMove;
    private int promotionPiece;
    private boolean isCastleMove;
    private boolean rightCastleDirection;
    private CastleState castleState;
    private boolean isEnPassantMove;
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

    public int getSource() {
        return source;
    }

    public int getTarget() {
        return target;
    }

    public int getPiece() {
        return piece;
    }

    public boolean getIsCaptureMove() {
        return isCaptureMove;
    }

    public int getPieceCaptured() {
        return pieceCaptured;
    }

    public boolean getIsPromotionMove() {
        return isPromotionMove;
    }

    public int getPromotionPiece() {
        return promotionPiece;
    }

    public boolean getIsCastleMove() {
        return isCastleMove;
    }

    public boolean getRightCastleDirection() {
        return rightCastleDirection;
    }

    public CastleState getCastleState() {
        return castleState;
    }

    public boolean getIsEnPassantMove() {
        return isEnPassantMove;
    }

    public void setSource(int val) {
        source = val;
    }

    public void setTarget(int val) {
        target = val;
    }

    public void setPiece(int val) {
        piece = val;
    }

    public void setIsCaptureMove(boolean val) {
        isCaptureMove = val;
    }

    public void setPieceCaptured(int val) {
        pieceCaptured = val;
    }

    public void setIsPromotionMove(boolean val) {
        isPromotionMove = val;
    }

    public void setPromotionPiece(int val) {
        promotionPiece = val;
    }

    public void setIsCastleMove(boolean val) {
        isCastleMove = val;
    }

    public void setRightCastleDirection(boolean val) {
        rightCastleDirection = val;
    }

    public void setCastleState(CastleState val) {
        castleState = val;
    }

    public void setIsEnPassantMove(boolean val) {
        isEnPassantMove = val;
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
