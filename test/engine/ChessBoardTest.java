package engine;
import chess.engine.CastleState;
import chess.engine.ChessBoard;

import chess.engine.Move;
import org.junit.jupiter.api.*;
import static chess.engine.BitBoardFunctions.orBitBoardArray;


import static org.junit.jupiter.api.Assertions.*;

class ChessBoardTest {

    @Test
    public void testVisualizeBoard() {
        ChessBoard chessBoard = new ChessBoard();

        String expectedBoard =
                """
                        Current Turn: White
                        r n b q k b n r
                        p p p p p p p p
                        . . . . . . . .
                        . . . . . . . .
                        . . . . . . . .
                        . . . . . . . .
                        P P P P P P P P
                        R N B Q K B N R
                        """;

        String actualBoard = chessBoard.toString();
        System.out.println("Starting Board: \n" + actualBoard);
        assertEquals(expectedBoard, actualBoard, "The chessboard visualization is incorrect");
    }
    @Test
    public void testMove() {
        ChessBoard chessBoard = new ChessBoard();

        Move move = new Move(55, 55-8,
                5, false, 0,
                false, 0, false, true, new CastleState(), false);
        assertEquals(move.source, 55);
        assertEquals(move.target, 55-8);
        assertEquals(move.piece, 5);
        assertFalse(move.isCaptureMove);
        assertEquals(move.pieceCaptured, 0);
        assertFalse(move.isPromotionMove);
        assertEquals(move.promotionPiece, 0);
        assertFalse(move.isCastleMove);
        assertTrue(move.rightCastleDirection);
        // test castle state
        assertTrue(move.castleState.blackCanLeftCastle);
        assertTrue(move.castleState.blackCanRightCastle);
        assertTrue(move.castleState.whiteCanLeftCastle);
        assertTrue(move.castleState.whiteCanRightCastle);
        assertFalse(move.isEnPassantMove);
    }
    @Test
    public void testGetLegalMoves() {
        ChessBoard chessBoard = new ChessBoard();

        String actualBoard = chessBoard.highlightPieceMoves(62);
        String expectedBoard =
                """
                        Current Turn: White
                        r n b q k b n r
                        p p p p p p p p
                        . . . . . . . .
                        . . . . . . . .
                        . . . . . . . .
                        . . . . . X . X
                        P P P P P P P P
                        R N B Q K B N R
                        """;
        assertEquals(expectedBoard, actualBoard);

        actualBoard = chessBoard.highlightPieceMoves(61);
        expectedBoard =
                """
                        Current Turn: White
                        r n b q k b n r
                        p p p p p p p p
                        . . . . . . . .
                        . . . . . . . .
                        . . . . . . . .
                        . . . . . . . .
                        P P P P P P P P
                        R N B Q K B N R
                        """;
        assertEquals(expectedBoard, actualBoard);

        actualBoard = chessBoard.highlightPieceMoves(52);
        expectedBoard =
                """
                        Current Turn: White
                        r n b q k b n r
                        p p p p p p p p
                        . . . . . . . .
                        . . . . . . . .
                        . . . . X . . .
                        . . . . X . . .
                        P P P P P P P P
                        R N B Q K B N R
                        """;
        assertEquals(expectedBoard, actualBoard);
    }
    @Test
    public void testKingCheck() {
        ChessBoard board = new ChessBoard();

        boolean actual = board.isSquareAttacked(60, orBitBoardArray(board.whiteBitBoards), orBitBoardArray(board.blackBitBoards));
        assertFalse(actual);

        board.makeMove(new Move(
                51, 31, 5, false, 0, false, 0, false, false, new CastleState(), false
        ));
        board.makeMove(new Move(
                12, 28, 5, false, 0, false, 0, false, false, new CastleState(), false
        ));
        board.makeMove(new Move(
                62, 46, 4, false, 0, false, 0, false, false, new CastleState(), false
        ));

        board.makeMove(new Move(
                5, 33, 3, false, 0, false, 0, false, false, new CastleState(), false
        ));

        actual = board.isSquareAttacked(60, orBitBoardArray(board.blackBitBoards), orBitBoardArray(board.whiteBitBoards));
        assertTrue(actual);
    }

    @Test
    public void testPin() { // edge case pinned knight can't move
        ChessBoard board = new ChessBoard();
        board.makeMove(new Move(
                51, 31, 5, false, 0, false, 0, false, false, new CastleState(), false
        ));
        board.makeMove(new Move(
                12, 28, 5, false, 0, false, 0, false, false, new CastleState(), false
        ));
        board.makeMove(new Move(
                57, 42, 4, false, 0, false, 0, false, false, new CastleState(), false
        ));

        board.makeMove(new Move(
                5, 33, 3, false, 0, false, 0, false, false, new CastleState(), false
        ));
        int[][] movePairs = board.getMovePairs();
        for (int[] move : movePairs) {
            assertNotEquals(42, move[0]); // make sure pinned knight can't move
        }
    }


}