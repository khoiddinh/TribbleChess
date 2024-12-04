package ChessGame;

public class ChessBoard {

    private static final long HEAD_INT = 0x8000000000000000L; // int that represents one at the 64th position
    private static final long TAIL_INT = 0x1L; // int that represents one at the 0th position
    private static final long FULL_BOARD = 0xFFFFFFFFFFFFFFFFL;
    private static final long LEFT_SIDE_BOARD = 0x8080808080808080L;
    private static final long RIGHT_SIDE_BOARD = 0x101010101010101L;
    private static final long TOP_SIDE_BOARD = 0xFF00000000000000L;
    private static final long BOTTOM_SIDE_BOARD = 0xFFL;
    private static final long EMPTY_BOARD = 0x0L;
    private static final char EMPTY_SQUARE = '.'; // char representation if no piece is there

    // bitboard is defined top right to bottom right: 100000000 -> 100 \n 000 \n 000 if 3x3
    private final long whiteKingBoard = 0x8;
    private final long blackKingBoard = 0x0800000000000000L;
    private final long whiteQueenBoard = 0x10;
    private final long blackQueenBoard = 0x1000000000000000L;
    private final long whiteRookBoard = 0x81;
    private final long blackRookBoard = 0x8100000000000000L;
    private final long whiteKnightBoard = 0x42;
    private final long blackKnightBoard = 0x4200000000000000L;
    private final long whiteBishopBoard = 0x24;
    private final long blackBishopBoard = 0x2400000000000000L;
    private final long whitePawnBoard = 0xFF00; // 0xFF -> 0xFF00 (each 0 adds 16 (2^4) to move up one row is 16*16 or 00)
    private final long blackPawnBoard = 0xFF000000000000L;
    
    private boolean isWhiteTurn;
    
    private long[] whiteBitBoards;
    private long[] blackBitBoards;


    public ChessBoard() {
        
        whiteBitBoards = new long[]{
                whiteKingBoard,
                whiteQueenBoard,
                whiteRookBoard,
                whiteKnightBoard,
                whiteBishopBoard,
                whitePawnBoard
        };
        
        blackBitBoards = new long[]{
                blackKingBoard,
                blackQueenBoard,
                blackRookBoard,
                blackKnightBoard,
                blackBishopBoard,
                blackPawnBoard
        };
        isWhiteTurn = true;
    }

    // returns a printable string of the current board
    public String visualizeBoard() {
        long[] bitboards = {
                whiteBitBoards[0], blackBitBoards[0],
                whiteBitBoards[1], blackBitBoards[1],
                whiteBitBoards[2], blackBitBoards[2],
                whiteBitBoards[3], blackBitBoards[3],
                whiteBitBoards[4], blackBitBoards[4],
                whiteBitBoards[5], blackBitBoards[5]
        };
        char[] symbols = {
                'K', 'k', // White King, Black King
                'Q', 'q', // White Queen, Black Queen
                'R', 'r', // White Rook, Black Rook
                'N', 'n', // White Knight, Black Knight
                'B', 'b', // White Bishop, Black Bishop
                'P', 'p'  // White Pawn, Black Pawn
        };
        StringBuilder returnString = new StringBuilder();
        for (int piecePos = 0; piecePos < 64; piecePos++) { // loop over piece position in string
            char symbolAtPos = EMPTY_SQUARE;
            for (int i = 0; i < bitboards.length; i++) {
                long bitboard = bitboards[i];
                char symbol = symbols[i];
                if ((bitboard & HEAD_INT) != 0) { // check if there is a piece at that position
                    if (symbolAtPos != EMPTY_SQUARE) { // sanity check to make sure there aren't two pieces at same pos
                        throw new RuntimeException("Two Pieces found at the same square");
                    }
                    symbolAtPos = symbol;

                }
                bitboards[i] = bitboard << 1; // left shift bitboard regardless of if there is a piece
            }
            returnString.append(symbolAtPos);
            if ((piecePos+1)%8 == 0) { // append newline after every row
                returnString.append('\n');
            }
            else {
                returnString.append(" "); // if not an end row character add spacing for looks
            }
        }
        return "Current Turn: " + (isWhiteTurn ? "White" : "Black") + "\n" + returnString;
    }

    // switches the turn of the game
    public void switchTurn() {
        isWhiteTurn = !isWhiteTurn;
    }

    // or operators all bitboards in array together
    private long orBitBoardArray(long[] bitBoards) {
        long result = EMPTY_BOARD;
        for (long bitBoard : bitBoards) {
            result |= bitBoard;
        }
        return result;
    }

    private long generateKingMoves(long[] bitBoards, long[] opponentBitBoards) {
        long kingBitBoard = bitBoards[0];
        long opponentBitBoard = orBitBoardArray(opponentBitBoards);
        long friendlyBitBoard = orBitBoardArray(bitBoards) - kingBitBoard; // piece bitboard not included
    }
}
