package org.cis1200.chess.engine;

public class CastleState {
    private boolean whiteCanRightCastle;
    private boolean whiteCanLeftCastle;
    private boolean blackCanRightCastle;
    private boolean blackCanLeftCastle;
    public CastleState() {
        whiteCanRightCastle = true;
        whiteCanLeftCastle = true;
        blackCanRightCastle = true;
        blackCanLeftCastle = true;
    }
    public CastleState(boolean whiteCanRightCastle, boolean whiteCanLeftCastle,
                       boolean blackCanRightCastle, boolean blackCanLeftCastle) {
        this.whiteCanRightCastle = whiteCanRightCastle;
        this.whiteCanLeftCastle = whiteCanLeftCastle;
        this.blackCanRightCastle = blackCanRightCastle;
        this.blackCanLeftCastle = blackCanLeftCastle;
    }

    public boolean whiteCanRightCastle() {
        return whiteCanRightCastle;
    }

    public boolean whiteCanLeftCastle() {
        return whiteCanLeftCastle;
    }

    public boolean blackCanRightCastle() {
        return blackCanRightCastle;
    }

    public boolean blackCanLeftCastle() {
        return blackCanLeftCastle;
    }

    public void setWhiteRightCastle(boolean val) {
        whiteCanRightCastle = val;
    }

    public void setWhiteLeftCastle(boolean val) {
        whiteCanLeftCastle = val;
    }

    public void setBlackRightCastle(boolean val) {
        blackCanRightCastle = val;
    }

    public void setBlackLeftCastle(boolean val) {
        blackCanLeftCastle = val;
    }

    public CastleState copy() {
        return new CastleState(whiteCanRightCastle, whiteCanLeftCastle,
                blackCanRightCastle, blackCanLeftCastle);
    }

    @Override
    public String toString() {
        return "(" + whiteCanRightCastle + ", " + whiteCanLeftCastle + ", "
                + blackCanRightCastle + ", " + blackCanLeftCastle + ")";
    }
}
