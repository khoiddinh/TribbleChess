package org.cis1200.chess.engine;

public class CastleState {
    public boolean whiteCanRightCastle;
    public boolean whiteCanLeftCastle;
    public boolean blackCanRightCastle;
    public boolean blackCanLeftCastle;
    public CastleState() {
        whiteCanRightCastle = true;
        whiteCanLeftCastle = true;
        blackCanRightCastle = true;
        blackCanLeftCastle = true;
    }
    public CastleState(boolean whiteCanRightCastle, boolean whiteCanLeftCastle, boolean blackCanRightCastle, boolean blackCanLeftCastle) {
        this.whiteCanRightCastle = whiteCanRightCastle;
        this.whiteCanLeftCastle = whiteCanLeftCastle;
        this.blackCanRightCastle = blackCanRightCastle;
        this.blackCanLeftCastle = blackCanLeftCastle;
    }

    public CastleState copy() {
        return new CastleState(whiteCanRightCastle, whiteCanLeftCastle, whiteCanRightCastle, whiteCanLeftCastle);
    }
}
