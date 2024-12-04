package tests;
import ChessGame.ChessBoard;

import org.junit.jupiter.api.*;
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
}