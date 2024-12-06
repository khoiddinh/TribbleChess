package org.cis1200.chess.engine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Stack;
import java.util.ArrayList;

import static org.cis1200.chess.engine.MoveGenerationPrecompute.*;

public class ChessBoard {

    private static final long HEAD_INT = 0x8000000000000000L; // int that represents one at the 64th position
    private static final long TAIL_INT = 0x1L; // int that represents one at the 0th position
    private static final long FULL_BOARD = 0xFFFFFFFFFFFFFFFFL;
    private static final long LEFT_SIDE_BOARD = 0x8080808080808080L;
    private static final long RIGHT_SIDE_BOARD = 0x101010101010101L;
    private static final long TOP_SIDE_BOARD = 0xFF00000000000000L;
    private static final long BOTTOM_SIDE_BOARD = 0xFFL;

    public static final long EMPTY_BOARD = 0x0L;
    public static final char EMPTY_SQUARE = '.'; // char representation if no piece is there
    
    private boolean isWhiteTurn;
    
    public long[] whiteBitBoards;
    public long[] blackBitBoards;

    private boolean whiteCanRightCastle;
    private boolean whiteCanLeftCastle;
    private boolean blackCanRightCastle;
    private boolean blackCanLeftCastle;

    private static final long WHITE_RIGHT_CASTLE_MASK = 0x6L;
    private static final long WHITE_LEFT_CASTLE_MASK = 0x30L;

    // NOTE: absolute right according to white POV
    private static final long BLACK_RIGHT_CASTLE_MASK = 0x600000000000000L;
    private static final long BLACK_LEFT_CASTLE_MASK = 0x3000000000000000L;

    private static final MoveGenerationPrecompute precompute = new MoveGenerationPrecompute();

    public Stack<Integer> moveStack;

    public ChessBoard() {

        // DEFAULT BOARD POSITION
        // bitboard is defined top right to bottom right: 100000000 -> 100 \n 000 \n 000 if 3x3
        final long whiteKingBoard = 0x8;
        final long blackKingBoard = 0x0800000000000000L;
        final long whiteQueenBoard = 0x10;
        final long blackQueenBoard = 0x1000000000000000L;
        final long whiteRookBoard = 0x81;
        final long blackRookBoard = 0x8100000000000000L;
        final long whiteKnightBoard = 0x42;
        final long blackKnightBoard = 0x4200000000000000L;
        final long whiteBishopBoard = 0x24;
        final long blackBishopBoard = 0x2400000000000000L;
        final long whitePawnBoard = 0xFF00; // 0xFF -> 0xFF00 (each 0 adds 16 (2^4) to move up one row is 16*16 or 00)
        final long blackPawnBoard = 0xFF000000000000L;

        // CASTLING DEFAULTS
        whiteCanRightCastle = true;
        whiteCanLeftCastle = true;
        blackCanRightCastle = true;
        blackCanLeftCastle = true;

        whiteBitBoards = new long[]{
            whiteKingBoard,
            whiteQueenBoard,
            whiteRookBoard,
            whiteBishopBoard,
            whiteKnightBoard,
            whitePawnBoard
        };

        blackBitBoards = new long[]{
                blackKingBoard,
                blackQueenBoard,
                blackRookBoard,
                blackBishopBoard,
                blackKnightBoard,
                blackPawnBoard
        };
        isWhiteTurn = true;
        moveStack = new Stack<Integer>();
    }

    public ChessBoard(String fen) {

    }

    // public access methods
    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    // -1 = black win, 0 = no winner, 1 = white win, 2 = draw
    public int checkWinner(ArrayList<Integer> legalMoves) {
        long whitePieces = orBitBoardArray(whiteBitBoards);
        long blackPieces = orBitBoardArray(blackBitBoards);
        ArrayList<Integer> moves = getLegalPossibleMoves();
        if (isWhiteTurn && moves.isEmpty()) {
            if (isKingInCheck(whiteBitBoards[0], whitePieces, blackPieces)) {
                return -1;
            } else { // stalemate, no other moves but king not in check
                return 2;
            }
        } else if (!isWhiteTurn && moves.isEmpty()) {
            if (isKingInCheck(blackBitBoards[0], whitePieces, blackPieces)) {
                return 1;
            }
        }
        return 0;
    }

    public char[][] getBoardArray() {
        char[][] returnBoard = new char[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                returnBoard[row][col] = EMPTY_SQUARE;
                // do white first
                char[] whitePieceMap = new char[]{'K', 'Q', 'R','B','N','P'};
                for (int piece = 0; piece < 6; piece++) {
                    // check if piece is at this pos
                    if ((whiteBitBoards[piece] & startingBitBoards[row*8+col]) != 0) {
                        returnBoard[row][col] = whitePieceMap[piece];
                        break;
                    }
                }
                char[] blackPieceMap = new char[]{'k', 'q', 'r', 'b', 'n', 'p'};
                for (int piece = 0; piece < 6; piece++) {
                    // check if piece is at this pos
                    if ((blackBitBoards[piece] & startingBitBoards[row*8+col]) != 0) {
                        returnBoard[row][col] = blackPieceMap[piece];
                        break;
                    }
                }
            }
        }
        return returnBoard;
    }

    // get a list of 2 element arrays: [source pos, target pos]
    public int[][] getMovePairs() {
        ArrayList<Integer> moves = getLegalPossibleMoves();
        int[][] movePairs = new int[moves.size()][];
        for (int i = 0; i < moves.size(); i++) {
            int move = moves.get(i);
            int source = (move & 0b111111);
            int target = (move & (0b111111 << 6)) >>> 6;
            movePairs[i] = new int[]{source, target};
        }
        return movePairs;
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
                'B', 'b', // White Bishop, Black Bishop
                'N', 'n', // White Knight, Black Knight
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

    public String highlightPieceMoves(int pos) {
        int[][] movePairs = getMovePairs();
        HashSet<Integer> moveTargetsFromPos = new HashSet<>();
        for (int[] movePair : movePairs) {
            if (movePair[0] == pos) { // if move piece at pos
                moveTargetsFromPos.add(movePair[1]); // add target
            }
        }

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
                'B', 'b', // White Bishop, Black Bishop
                'N', 'n', // White Knight, Black Knight
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

            if (moveTargetsFromPos.contains(piecePos)) {
                symbolAtPos = 'X';
            }

            returnString.append(symbolAtPos);
            if ((piecePos+1) % 8 == 0) { // append newline after every row
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
    public long orBitBoardArray(long[] bitBoards) {
        long result = EMPTY_BOARD;
        for (long bitBoard : bitBoards) {
            result |= bitBoard;
        }
        return result;
    }

    // gets the top left zero indexed position of least sig bit
    public int getPosOfLeastSigBit(long n) {
        return 63-Long.numberOfTrailingZeros(n);
    }

    private void printBinary(int n) {
        System.out.println(Integer.toBinaryString(n));
    }

    // note: castle source and target follow the king
    // note: enPassant source and target follow the capturing pawn
    public int encodeMove(int source, int target, int piece,
                          boolean capture, int pieceCaptured, boolean promotion,
                          int promotionPiece, boolean castleMove,
                          int castleDirection, int castleState, boolean enPassant) {
        int encodedMove = 0;
        encodedMove |= source;
        encodedMove |= target << 6;
        encodedMove |= piece << 12;
        encodedMove |= (capture ? 1 : 0) << 15;
        encodedMove |= pieceCaptured << 16;
        encodedMove |= (promotion ? 1 : 0) << 19;
        encodedMove |= promotionPiece << 20;
        encodedMove |= (castleMove ? 1 : 0) << 22;
        encodedMove |= castleDirection << 23;
        encodedMove |= castleState << 24;
        encodedMove |= (enPassant ? 1 : 0) << 28;
        return encodedMove;
    }

    // gets binary trace of piece attacks
    // NOTE: doesn't handle enPassant, castle, or promotion
    private long getTraceOfPiece(int pos, int piece, long friendlyBitBoard, long opponentBitBoard) {
        long moveMask = 0;
        long blockerBitBoard = friendlyBitBoard | opponentBitBoard;
        switch (piece) {
            case 0: // king
                moveMask = kingAttackMasks[pos];
                break;
            case 1: // queen
                moveMask = getSlidingAttackWithBlockers(
                        pos, blockerBitBoard, 1);
                break;
            case 2: // rook
                moveMask = getSlidingAttackWithBlockers(pos, blockerBitBoard, 2);
                break;
            case 3: // bishop
                moveMask = getSlidingAttackWithBlockers(pos, blockerBitBoard, 3);

                break;
            case 4: // knight
                moveMask = knightAttackMasks[pos];
                break;
            case 5: // pawn
                // returns both moves and attacks
                // does not handle enPassant (handled in getPossibleLegalMoves)
                if (isWhiteTurn) {
                    moveMask = whitePawnMoveMasks[pos];
                    moveMask |= (whitePawnAttackMasks[pos] & opponentBitBoard); // only can go there if takes
                } else { // black pawn moves
                    moveMask = blackPawnMoveMasks[pos];
                    moveMask |= (blackPawnAttackMasks[pos] & opponentBitBoard);
                }
                break;
        }
        moveMask ^= (moveMask & friendlyBitBoard);
        return moveMask;
    }

    // TODO: speedup -> make isCastleSquareAttackedFunction
    // TODO: UNIT TESTS
    public boolean isSquareAttacked(int pos, long friendly, long opponentBitBoard) {
        long blockerBitBoard = friendly | opponentBitBoard;

        // array of mask of how each piece could attack pos
        long[] potentialAttacks = new long[6];
        for (int piece = 0; piece < 6; piece++) {
            switch (piece) {
                case 0: // king
                    potentialAttacks[piece] = kingAttackMasks[pos];
                    break;
                case 1: // queen
                case 2: // rook
                case 3: // bishop
                    potentialAttacks[piece] = getSlidingAttackWithBlockers(pos, blockerBitBoard, piece);
                    break;
                case 4: // knight
                    potentialAttacks[piece] = knightAttackMasks[pos];
                    break;
                case 5: // pawn
                    if (isWhiteTurn && ((startingBitBoards[pos] & TOP_MASK) == 0)) { // not on top row
                        potentialAttacks[piece] =
                                startingBitBoards[pos-9] | startingBitBoards[pos-7]; // up left and up right
                    } else if (!isWhiteTurn && (((startingBitBoards[pos] & BOTTOM_MASK) == 0))) {                         // not on bottom row;
                        potentialAttacks[piece] = startingBitBoards[pos+7] | startingBitBoards[pos+9];
                    }
                    break;
            }
        }
        long[] opponentBitBoardList = isWhiteTurn ? blackBitBoards : whiteBitBoards;
        for (int piece = 0; piece < 6; piece++) {
            // check if each piece is on the attack directions to pos
            if ((opponentBitBoardList[piece] & potentialAttacks[piece]) != 0) { // piece attacking that square
                return true;
            }
        }
        return false;
    }

    // takes the current castle state and encodes it into int format
    private int encodeCastleState() {
        int encodedState = blackCanLeftCastle ? 1 : 0;
        encodedState |= (blackCanRightCastle ? 1 : 0) << 1;
        encodedState |= (whiteCanLeftCastle ? 1 : 0) << 2;
        encodedState |= (whiteCanRightCastle ? 1 : 0) << 3;
        return encodedState;
    }

    // gets if king is currently in check (includes but doesn't distinguish mate)
    public boolean isKingInCheck(long kingBitBoard, long friendlyBitBoard, long opponentBitBoard) {
        int kingPos = getPosOfLeastSigBit(kingBitBoard);
        return isSquareAttacked(kingPos, friendlyBitBoard, opponentBitBoard);
    }

    private int findPieceAtPos(int pos, boolean findWhitePiece) {
        long[] bitBoardList = findWhitePiece ? whiteBitBoards : blackBitBoards;
        long posMask = startingBitBoards[pos];
        for (int piece = 0; piece < 6; piece++) {
            if ((posMask & bitBoardList[piece]) != 0) {
                return piece;
            }
        }
        throw new IllegalArgumentException("No piece at Pos");
    }

    // TODO: TEST
    private ArrayList<Integer> filterLegalMoves(ArrayList<Integer> moves) {
        long[] bitBoardList = isWhiteTurn ? whiteBitBoards : blackBitBoards;
        ArrayList<Integer> legalMoves = new ArrayList<>();
        for (int move : moves) {
            makeMove(move);
            switchTurn(); // because make move switches turn,
            // but we want to check king check with respect to previous color
            // check friendly after switch turn because eval same side
            long[] friendlyBitBoardList = isWhiteTurn ? whiteBitBoards : blackBitBoards;
            long[] opponentBitBoardList = isWhiteTurn ? blackBitBoards : whiteBitBoards;
            long friendlyBitBoard = orBitBoardArray(friendlyBitBoardList);
            long opponentBitBoard = orBitBoardArray(opponentBitBoardList);
            if (!isKingInCheck(bitBoardList[0], friendlyBitBoard, opponentBitBoard)) { // legal
                legalMoves.add(move);
            }
            switchTurn(); // switch turn back
            undoLastMove();
        }
        return legalMoves;
    }

    // move encoding (in top bottom order of most sig to least sig digit):
    // 1 bit enPassant (1: enPassant, 0: no enPassant) IF enPASSANT, CAPTURE FLAG TRUE
    // 4 bits: PREVIOUS castle state (what was the castle state BEFORE playing this move)
    // order of castle state: [whiteRightCastle, whiteLeftCastle, blackRightCastle, blackLeftCastle] 1 = true
    // 1 bit: castle direction (0: right, 1: left) IGNORE UNLESS CASTLE FLAG
    // 1 bit: castleMove (is this move a castle): (boolean) 1 = yes, 0 = no
    // 2 bits: promotion piece (00: queen, 01: rook, 10: bishop, 11: knight) IGNORE UNLESS PROMOTION FLAG TRUE
    // 1 bit: promotion
    // 3 bits: piece captured IGNORE UNLESS CAPTURE FLAG TRUE can't really be king but
    // 1 bit: capture (1 = yes, 0 = no)
    // 3 bits: piece
    // 6 bits: target square
    // 6 bits: source square
    // NOTE: FOR NOW, MOVE STORES CASTLE STATE BEFORE MOVE FOR UNDO
    public ArrayList<Integer> getLegalPossibleMoves() {
        long[] bitBoardList = isWhiteTurn ? whiteBitBoards : blackBitBoards;

        long opposingBitBoard = orBitBoardArray(
                isWhiteTurn ? blackBitBoards : whiteBitBoards);
        long friendlyBitBoard = orBitBoardArray(bitBoardList);
        long blockerBitBoard = friendlyBitBoard | opposingBitBoard;

        ArrayList<Integer> possibleMoves = new ArrayList<>();

        for (int piece = 0; piece < bitBoardList.length; piece++) { // loop over each piece bitboard
            long pieceBitBoard = bitBoardList[piece]; // bitboard with just this piece (could be multiple pieces)
            while (pieceBitBoard != 0) {
                // check piece bitBoardList[i] at pos for moves
                int pos = getPosOfLeastSigBit(pieceBitBoard);
                // get move mask (trace attack) of this piece at this pos
                long moveMask = getTraceOfPiece(pos, piece, friendlyBitBoard, opposingBitBoard);

                // CHECK AND ADD SPECIAL MOVES
                // add king castling
                if (piece == 0) {
                    // check if white can right castle
                    if (isWhiteTurn && whiteCanRightCastle &&
                            ((blockerBitBoard & WHITE_RIGHT_CASTLE_MASK) == 0) &&
                            !isSquareAttacked(60, friendlyBitBoard, opposingBitBoard) && // can't castle in check
                            !isSquareAttacked(61, friendlyBitBoard, opposingBitBoard) &&
                            !isSquareAttacked(62, friendlyBitBoard, opposingBitBoard)) {
                        possibleMoves.add(
                                encodeMove(60, 62,piece, false,
                                        0, false, 0,
                                        true, 0, encodeCastleState(),
                                        false)
                        );
                    }
                    // check if white can left castle
                    if (isWhiteTurn && whiteCanLeftCastle &&
                            ((blockerBitBoard & WHITE_LEFT_CASTLE_MASK) == 0) &&
                            !isSquareAttacked(60, friendlyBitBoard, opposingBitBoard) &&
                            !isSquareAttacked(59, friendlyBitBoard, opposingBitBoard) &&
                            !isSquareAttacked(58, friendlyBitBoard, opposingBitBoard)) {
                        possibleMoves.add(
                                encodeMove(60, 58, piece, false,
                                        0, false, 0,
                                        true, 1, encodeCastleState(), false)
                        );
                    }
                    // check if black can right castle
                    if (!isWhiteTurn && blackCanRightCastle &&
                            ((blockerBitBoard & BLACK_RIGHT_CASTLE_MASK) == 0) &&
                            !isSquareAttacked(4, friendlyBitBoard, opposingBitBoard) &&
                            !isSquareAttacked(5, friendlyBitBoard, opposingBitBoard) &&
                            !isSquareAttacked(6, friendlyBitBoard, opposingBitBoard)) {
                        possibleMoves.add(
                                encodeMove(4, 6,piece, false, 0,
                                        false, 0,
                                        true, 0, encodeCastleState(),
                                        false)
                        );
                    }
                    // check if black can left castle
                    if (!isWhiteTurn && blackCanLeftCastle &&
                            ((blockerBitBoard & BLACK_LEFT_CASTLE_MASK) == 0) &&
                            !isSquareAttacked(4, friendlyBitBoard, opposingBitBoard) &&
                            !isSquareAttacked(3, friendlyBitBoard, opposingBitBoard) &&
                            !isSquareAttacked(2, friendlyBitBoard, opposingBitBoard)) {
                        possibleMoves.add(
                                encodeMove(4, 2,piece,
                                        false, 0,
                                        false, 0,
                                        true, 1, encodeCastleState(),
                                        false)
                        );
                    }
                }

                // handle pawn enPassant ONLY
                // adds additional enPassant moves
                if (piece == 5) {
                    if (!moveStack.isEmpty()) {
                        int prevMove = moveStack.peek();
                        int prevMoveSource = prevMove & 0b111111;
                        int prevMoveTarget = (prevMove & (0b111111 << 6)) >>> 6;
                        int prevMovePiece = (prevMove & (0b111 << 12)) >>> 12;
                        if (prevMovePiece == 5 &&
                                (Math.abs(prevMoveSource-prevMoveTarget) == 16)) { // if pawn and two square move
                            if (((startingBitBoards[pos] & LEFT_SIDE_BOARD) == 0)) { // if pawn not on left edge
                                // calculate left side enPassant
                                if (pos-1 == prevMoveTarget) { // if prev move directly left of curr pawn pos
                                    possibleMoves.add(encodeMove(pos, isWhiteTurn ? pos-9 : pos + 7, 5,
                                            true, 5,
                                            false, 0,
                                            false, 0, encodeCastleState(),
                                            true));
                                }
                            }
                            if (((startingBitBoards[pos] & RIGHT_SIDE_BOARD) == 0)) { // if pawn not on right edge
                                // calculate right side enPassant
                                if (pos+1 == prevMoveTarget) { // if prev move directly left of curr pawn pos
                                    possibleMoves.add(encodeMove(pos, isWhiteTurn ? pos-7 : pos + 9, 5,
                                            true, 5,
                                            false, 0,
                                            false, 0, encodeCastleState(),
                                            true));
                                }
                            }

                        }
                    }
                }

                // ADD REGULAR MOVES
                while (moveMask != 0) {
                    int targetPos = getPosOfLeastSigBit(moveMask);

                    // capture logic
                    boolean isCaptureMove = (startingBitBoards[targetPos] & opposingBitBoard) != 0;
                    int capturedPiece = 0;
                    if (isCaptureMove) { // if capturing a piece, find the piece we're capturing
                        capturedPiece = findPieceAtPos(targetPos, !isWhiteTurn); // if white turn, find black piece
                    }

                    // pawn promotion logic to add promotion flag along with non queen promotions
                    // capture flag even in the case of promotion is already handled in the above block
                    boolean isPromotion = false;
                    if (piece == 5) {
                        if (isWhiteTurn && ((startingBitBoards[targetPos] & TOP_MASK) != 0)) { // white promotion
                            for (int promotionPiece = 1; promotionPiece < 4; promotionPiece++) { // queen added by default
                                int move = encodeMove(pos, targetPos, piece,
                                        isCaptureMove, capturedPiece,
                                        true, promotionPiece,
                                        false, 0, encodeCastleState(),
                                        false);
                                possibleMoves.add(move);
                            }
                            isPromotion = true;
                        }
                        else if (!isWhiteTurn && ((startingBitBoards[targetPos] & BOTTOM_MASK) != 0)) { // black promotion
                            for (int promotionPiece = 1; promotionPiece < 4; promotionPiece++) { // queen added by default
                                int move = encodeMove(pos, targetPos, piece,
                                        isCaptureMove, capturedPiece,
                                        true, promotionPiece,
                                        false, 0, encodeCastleState(),
                                        false);
                                possibleMoves.add(move);
                            }
                            isPromotion = true;
                        }
                    }

                    // king can't move into check functionality
                    if (!(piece == 0 && isSquareAttacked(targetPos, friendlyBitBoard, opposingBitBoard)) &&
                            !(piece == 5 && isCaptureMove && ((Math.abs(targetPos-pos) % 8) == 0))) { // pawn can't capture on same column

                        // if promotion, queen added by default, block above adds rest
                        int move = encodeMove(pos, targetPos, piece,
                                isCaptureMove, capturedPiece,
                                isPromotion, 0,
                                false, 0,
                                encodeCastleState(), false);
                        possibleMoves.add(move);
                    }
                    moveMask ^= startingBitBoards[targetPos]; // remove this move from move mask
                    // cont: (the moves that we still have to convert and encode)
                }
                pieceBitBoard ^= startingBitBoards[pos]; // remove piece from bitboard and process next
            }
        }
        ArrayList<Integer> filteredMoves = filterLegalMoves(possibleMoves);
        return filteredMoves;
    }

    // plays move, updates bitboards, and switches turn
    // assumes valid move
    // TODO: UNIT TESTS
    public void makeMove(int move) {
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

        long[] bitBoardList = isWhiteTurn ? whiteBitBoards : blackBitBoards;
        long[] opponentBitBoardList = isWhiteTurn ? blackBitBoards : whiteBitBoards;
        // remove piece at source location
        bitBoardList[piece] ^= startingBitBoards[source];
        // add at new target location
        bitBoardList[piece] |= startingBitBoards[target];
        // if capture move, update the capture bitboard in opponent bitboard
        if (capture && !enPassant) { // don't handle enPassant b/c target isn't loc of enemy pawn
            opponentBitBoardList[pieceCaptured] ^= startingBitBoards[target]; // remove captured piece
        }
        if (enPassant) {
            // if white turn, enPassant pawn is below (+) if black, enPassant pawn is above
            opponentBitBoardList[5] ^= startingBitBoards[isWhiteTurn ? target + 8 : target - 8];
        }
        // if promotion, replace the pawn (that we already moved) with the promoted piece
        if (promotion) {
            bitBoardList[piece] ^= startingBitBoards[target]; // remove pawn
            bitBoardList[promotionPiece] |= startingBitBoards[target]; // replace with promoted piece
        }
        // if castleMove, move the rook since we already moved the king above
        if (castleMove) {
            switch (castleDirection) {
                case 0: // right castle
                    bitBoardList[2] ^= startingBitBoards[isWhiteTurn ? 63 : 7];
                    bitBoardList[2] |= startingBitBoards[isWhiteTurn ? 61 : 5];
                    break;
                case 1: // left castle
                    bitBoardList[2] ^= startingBitBoards[isWhiteTurn ? 56 : 0];
                    bitBoardList[2] |= startingBitBoards[isWhiteTurn ? 59 : 3];
            }
            // update the castle fields
            if (isWhiteTurn) {
                whiteCanLeftCastle = false;
                whiteCanRightCastle = false;
            } else { // black
                blackCanLeftCastle = false;
                blackCanRightCastle = false;
            }
        }

        // if move isn't a castle but could affect castle state
        // (if you move the king or rook)
        if ((piece == 0 || piece == 2) && !castleMove) {
            switch (piece) {
                case 0: // king
                    if (isWhiteTurn) { // if king moves, castle not valid
                        whiteCanLeftCastle = false;
                        whiteCanRightCastle = false;
                    } else {
                        blackCanLeftCastle = false;
                        blackCanRightCastle = false;
                    }
                    break;
                case 2: // rook
                    if (isWhiteTurn) {
                        if (source == 63) { // white right rook
                            whiteCanRightCastle = false;
                        } else if (source == 56) { // white left rook
                            whiteCanLeftCastle = false;
                        }
                    } else { // black turn
                        if (source == 7) { // black right rook
                            blackCanRightCastle = false;
                        } else if (source == 0) { // black left rook
                            blackCanLeftCastle = false;
                        }
                    }
                    break;
            }
        }
        // Do nothing with castle state, this is solely for undoMove function



        // add move to moveStack
        moveStack.add(move);

        // switch turn
        switchTurn();
    }

    public void undoLastMove() {
        int move = moveStack.pop();
        switchTurn(); // switch turn first
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

        long[] bitBoardList = isWhiteTurn ? whiteBitBoards : blackBitBoards;
        long[] opponentBitBoardList = isWhiteTurn ? blackBitBoards : whiteBitBoards;
        // if promotion, remove promoted piece from target
        if (promotion) {
            bitBoardList[promotionPiece] ^= startingBitBoards[target];
        } else { // otherwise
            // remove piece from target
            bitBoardList[piece] ^= startingBitBoards[target];
        }
        // add it back to the original place
        bitBoardList[piece] |= startingBitBoards[source];
        // if captured, add the enemy piece back to its spot
        if (capture && !enPassant) { // don't consider enPassant, handle replace below in the enPassant block
            opponentBitBoardList[pieceCaptured] |= startingBitBoards[target];
        }
        // put the captured pawn back, already moved capturing pawn back
        if (enPassant) {
            // if white turn, enPassant pawn is below (+) if black, enPassant pawn is above
            opponentBitBoardList[5] |= startingBitBoards[isWhiteTurn ? target + 8 : target - 8];
        }

        // if castle, move rook back and fix castle fields
        if (castleMove) {
            switch (castleDirection) {
                case 0: // right castle
                    bitBoardList[2] ^= startingBitBoards[isWhiteTurn ? 61 : 5];
                    bitBoardList[2] |= startingBitBoards[isWhiteTurn ? 63 : 7];
                    break;
                case 1: // left castle
                    bitBoardList[2] ^= startingBitBoards[isWhiteTurn ? 59 : 3];
                    bitBoardList[2] |= startingBitBoards[isWhiteTurn ? 56 : 0];
            }

        }
        // fix the castle fields (regardless of if castle move
        // since could be rook or king move that changed it last move)
        blackCanLeftCastle = (castleState & 1) == 1;
        blackCanRightCastle = ((castleState & (1 << 1)) >>> 1) == 1;
        whiteCanLeftCastle = ((castleState & (1 << 2)) >>> 2) == 1;
        whiteCanRightCastle = ((castleState & (1 << 3)) >>> 3) == 1;
    }

    // TODO: implement
    public void reset() {

    }
}
