package org.cis1200.test.engine;
import org.cis1200.chess.engine.ChessBoard;

import org.junit.jupiter.api.*;

import java.util.ArrayList;

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

        String actualBoard = chessBoard.visualizeBoard();
        System.out.println("Starting Board: \n" + actualBoard);
        assertEquals(expectedBoard, actualBoard, "The chessboard visualization is incorrect");
    }
    @Test
    public void testEncodeMove() {
        ChessBoard chessBoard = new ChessBoard();

        int actual = chessBoard.encodeMove(55, 55-8,
                5, false, 0,
                false, 0, false, 0, 0, false);
        int expected = 0b0_0_0_0_0_0_0_0_101_101111_110111;
        System.out.println(Integer.toBinaryString(actual));
        assertEquals(expected, actual);
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

        boolean actual = board.isSquareAttacked(60, board.orBitBoardArray(board.whiteBitBoards), board.orBitBoardArray(board.blackBitBoards));
        assertFalse(actual);

        board.makeMove(251681076);
        board.makeMove(251680524);
        board.makeMove(251677566);
        board.makeMove(251675777);
        board.makeMove(251672189);

        actual = board.isSquareAttacked(4, board.orBitBoardArray(board.blackBitBoards), board.orBitBoardArray(board.whiteBitBoards));
        assertFalse(actual);
    }

    @Test
    public void testPin() {
        ChessBoard board = new ChessBoard();

        board.makeMove(251681076);
        board.makeMove(251680524);
        board.makeMove(251677566);
        board.makeMove(251675777);
        board.makeMove(251672189);
        board.makeMove(251680459);
        board.makeMove(252036909);
        board.getLegalPossibleMoves();

        //board.makeMove(251971346); // knight move king exposed
        boolean actual = board.isSquareAttacked(4, board.orBitBoardArray(board.blackBitBoards),
                board.orBitBoardArray(board.whiteBitBoards));
        assertTrue(actual);
    }
}