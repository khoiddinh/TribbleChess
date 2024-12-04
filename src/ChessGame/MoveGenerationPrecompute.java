package ChessGame;

public class MoveGenerationPrecompute {

    public static final long LEFT_MASK = 0x8080808080808080L;
    public static final long RIGHT_MASK = 0x101010101010101L;
    public static final long TOP_MASK = 0xFF00000000000000L;
    public static final long BOTTOM_MASK = 0xFFL;
    public static final long EDGE_MASK = LEFT_MASK | RIGHT_MASK | TOP_MASK | BOTTOM_MASK;

    public static long[] startingBitBoards;

    public static long[] kingAttackMasks;
    public static long[] knightAttackMasks;

    public static long[] whitePawnMoveMasks;
    public static long[] whitePawnAttackMasks;

    public static long[] blackPawnMoveMasks;
    public static long[] blackPawnAttackMasks;

    // sliding pieces are implemented differently for magic bitboards
    // the masks don't include edges since sliding pieces will
    // always be able to attack them
    // (unless there is a blocker in the way)
    // TODO: Implement Magic BitBoards

    public static long[] rookAttackMasks;
    public static long[] bishopAttackMasks;
    public static long[] queenAttackMasks;

    // bitshift amounts corresponding to each direction
    // NOTE: left shift vs right shift
    public static final int[] RAY_DIRECTIONS = {1, 9, 8, 7};
    public static final long[] LEFT_END_MASKS =
            {LEFT_MASK, LEFT_MASK | TOP_MASK, TOP_MASK, RIGHT_MASK | TOP_MASK};
    public static final long[] RIGHT_END_MASKS =
            {RIGHT_MASK, RIGHT_MASK | BOTTOM_MASK, BOTTOM_MASK, LEFT_MASK | BOTTOM_MASK};

    public static long[][] RAYS; // [direction][position]
    // left = 0, up left = 1, up = 2, up right = 3 (left shifts)
    // right = 4, down right = 5, down = 6, down left = 7 (right shifts)
    // position is square (n) where 0 <= n < 64

    // Magic Bitboard Lookup Tables
    public static long[][] BISHOP_TABLE;
    public static long[][] ROOK_TABLE;
    public static long[][] QUEEN_TABLE;

    public MoveGenerationPrecompute() {
        startingBitBoards = new long[64];
        for (int i = 0; i < 64; i++) {
            startingBitBoards[i] = 0x1L << 63-i;
        }
        kingAttackMasks = new long[64];
        for (int i = 0; i < 64; i++) {
            kingAttackMasks[i] = generateKingAttackMask(i);
        }
        knightAttackMasks = new long[64];
        for (int i = 0; i < 64; i++) {
            knightAttackMasks[i] = generateKnightAttackMask(i);
        }
        whitePawnAttackMasks = new long[64];
        for (int i = 0; i < 64; i++) {
            whitePawnAttackMasks[i] = generateWhitePawnAttackMask(i);
        }
        whitePawnMoveMasks = new long[64];
        for (int i = 0; i < 64; i++) {
            whitePawnMoveMasks[i] = generateWhitePawnMoveMask(i);
        }
        blackPawnAttackMasks = new long[64];
        for (int i = 0; i < 64; i++) {
            blackPawnAttackMasks[i] = generateBlackPawnAttackMask(i);
        }
        blackPawnMoveMasks = new long[64];
        for (int i = 0; i < 64; i++) {
            blackPawnMoveMasks[i] = generateBlackPawnMoveMask(i);
        }

        rookAttackMasks = new long[64];
        for (int i = 0; i < 64; i++) {
            rookAttackMasks[i] = generateRookAttackMask(i);
        }

        bishopAttackMasks = new long[64];
        for (int i = 0; i < 64; i++) {
            bishopAttackMasks[i] = generateBishopAttackMask(i);
        }

        queenAttackMasks = new long[64];
        for (int i = 0; i < 64; i++) {
            queenAttackMasks[i] = generateQueenAttackMask(i);
        }

        RAYS = generateRays();
    }

    private static int getPosOfLeastSigBit(long n) {
        if (n == 0) return -1;
        return 63-Long.numberOfTrailingZeros(n);
    }

    public static void printBitBoard(long val) {
        String s = Long.toBinaryString(val);
        StringBuilder r = new StringBuilder();
        r.append("0".repeat(64 - s.length()));
        for (int i = 0; i < s.length(); i++) {
            r.append(s.charAt(i));
        }
        s = r.toString();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            result.append(s.charAt(i));
            if (((i + 1) % 8) == 0) {
                result.append('\n');
            }
        }
        System.out.println("-----");
        System.out.println(result);
        System.out.println("-----");
    }

    private long generateKingAttackMask(int square) {
        long bitBoard = startingBitBoards[square];
        long attackMask = 0;
        if ((bitBoard & LEFT_MASK) == 0) { // if not on left edge
            attackMask |= bitBoard << 1;
        }
        if (((bitBoard & (LEFT_MASK | TOP_MASK)) == 0)) { // if not on left or top edge
            attackMask |= bitBoard << 9;
        }
        if (((bitBoard & TOP_MASK)) == 0) { // if not on top edge
            attackMask |= bitBoard << 8;
        }
        if (((bitBoard & (RIGHT_MASK | TOP_MASK))) == 0) { // if not on right or top edge
            attackMask |= bitBoard << 7;
        }
        if (((bitBoard & RIGHT_MASK)) == 0) { // if not on right edge
            attackMask |= bitBoard >>> 1; // unsigned shift so sign doesn't propagate
        }
        if (((bitBoard & (RIGHT_MASK | BOTTOM_MASK))) == 0) {
            attackMask |= bitBoard >>> 9;
        }
        if (((bitBoard & BOTTOM_MASK)) == 0) {
            attackMask |= bitBoard >>> 8;
        }
        if ((bitBoard & (LEFT_MASK | BOTTOM_MASK)) == 0) {
            attackMask |= bitBoard >>> 7;
        }
        return attackMask;
    }

    private long generateKnightAttackMask(int square) {
        long attackMask = 0;
        long bitBoard = startingBitBoards[square];
        // Left edge masks (prevent wraparound)
        long fileA = LEFT_MASK; // File A
        long fileAB = 0xC0C0C0C0C0C0C0C0L; // Files A and B
        long fileH = RIGHT_MASK; // File H
        long fileGH = 0x303030303030303L; // Files G and H

        // Mask for top and bottom ranks
        long topRank = TOP_MASK;
        //printBitBoard(fileAB | topRank);
        long bottomRank = BOTTOM_MASK;
        long top2Ranks = 0xFFFF000000000000L; // Exclude ranks 7 and 8
        long bottom2Ranks = 0xFFFFL; // Exclude ranks 1 and 2

        // Knight moves: 8 directions
        // Up moves (ranks increasing)
        if ((bitBoard & (fileA | top2Ranks)) == 0) attackMask |= (bitBoard << 17); // up 2, left 1
        if ((bitBoard & (fileAB | topRank)) == 0) attackMask |= (bitBoard << 10); // up 1, left 2
        if ((bitBoard & (fileH | top2Ranks)) == 0) attackMask |= (bitBoard << 15); // up 2, right 1
        if ((bitBoard & (fileGH | topRank)) == 0) attackMask |= (bitBoard << 6); // up 1, right 2

        // Down moves (ranks decreasing)
        if ((bitBoard & (fileA | bottom2Ranks)) == 0) attackMask |= (bitBoard >>> 15); // down 2, left 1
        if ((bitBoard & (fileAB | bottomRank)) == 0) attackMask |= (bitBoard >>> 6); // down 1, left 2
        if ((bitBoard & (fileH | bottom2Ranks)) == 0) attackMask |= (bitBoard >>> 17); // down 2, right 1
        if ((bitBoard & (fileGH | bottomRank)) == 0) attackMask |= (bitBoard >>> 10); // down 1, right 2

        return attackMask;
    }

    public long generateWhitePawnMoveMask(int square) {
        long bitBoard = startingBitBoards[square];
        // TODO: implement promotion logic in getPossibleMoves
        if ((bitBoard & TOP_MASK) != 0) {
            return bitBoard; // if at the final row, promote, therefore no valid moves
        }
        long mask = bitBoard << 8;
        if ((bitBoard & (BOTTOM_MASK << 8)) != 0) { // if on second row
            mask |= bitBoard << 16;
        }
        return mask;
    }

    public long generateWhitePawnAttackMask(int square) {
        long bitBoard = startingBitBoards[square];
        // TODO: implement promotion logic in getPossibleMoves
        if ((bitBoard & (TOP_MASK | BOTTOM_MASK)) != 0) {
            // if at first row, impossible
            return bitBoard; // if at final row, promote, no valid moves

        }
        long mask = 0;
        if ((bitBoard & LEFT_MASK) == 0) mask |= bitBoard << 9; // if not on left edge
        if ((bitBoard & RIGHT_MASK) == 0) mask |= bitBoard << 7; // if not on right edge
        return mask;
    }

    public long generateBlackPawnMoveMask(int square) {
        long bitBoard = startingBitBoards[square];
        // TODO: implement promotion logic in getPossibleMoves
        if ((bitBoard & (BOTTOM_MASK | TOP_MASK)) != 0) {
            return bitBoard; // if at the final row, promote, therefore no valid moves
        }
        long mask = bitBoard >>> 8;
        if ((bitBoard & (TOP_MASK >>> 8)) != 0) { // if on second row wrt black
            mask |= bitBoard >>> 16;
        }
        return mask;
    }

    public long generateBlackPawnAttackMask(int square) {
        long bitBoard = startingBitBoards[square];
        // TODO: implement promotion logic in getPossibleMoves
        // TODO: have sanity check in get possible moves if pawn is in first row or smth
        if ((bitBoard & (BOTTOM_MASK | TOP_MASK)) != 0) {
            return bitBoard; // if at final row, promote, no valid moves
        }
        long mask = 0;
        if ((bitBoard & LEFT_MASK) == 0) mask |= bitBoard >>> 9; // if not on left edge
        if ((bitBoard & RIGHT_MASK) == 0) mask |= bitBoard >>> 7; // if not on right edge
        return mask;
    }

    public long generateRookAttackMask(int square) {
        long attackMask = 0;
        int rank = square / 8; // rank is rows, indexed top left
        int file = square % 8; // file is cols, indexed top left

        // Horizontal (left and right)
        attackMask |= BOTTOM_MASK << ((8 * (7 - rank)));

        // Vertical (up and down)
        attackMask ^= LEFT_MASK >>> file; // subtracts piece position (overlap)
        return attackMask;
    }

    // TODO: write tests
    public long generateBishopAttackMask(int square) {
        long attackMask = 0;
        for (int direction = 0; direction < 8; direction++) {
            attackMask |= RAYS[direction][square];
        }
        return attackMask;
    }

    // TODO: write tests
    public long generateQueenAttackMask(int square) {
        return generateBishopAttackMask(square) | generateRookAttackMask(square);
    }

    private long[][] generateRays() {
        // see direction key above
        // left shifts
        long[][] rayResult = new long[8][64];
        for (int direction = 0; direction < 4; direction++) {
            // border to stop at (finish curr pos but dont go on)
            long endMask = LEFT_END_MASKS[direction];
            for (int pos = 0; pos < 64; pos++) {
                long rayMask = 0;
                long bitBoard = startingBitBoards[pos];
                while ((endMask & bitBoard) == 0) {
                    rayMask |= bitBoard; // add curr ray pos to mask
                    bitBoard <<= RAY_DIRECTIONS[direction]; // shift to next pos
                }
                rayMask |= bitBoard; // add pos on the edge
                rayMask ^= startingBitBoards[pos]; // TODO: test; since mask includes piece, remove it
                rayResult[direction][pos] = rayMask;
            }
        }
        // right shifts
        for (int direction = 0; direction < 4; direction++) {
            // border to stop at (finish curr pos but dont go on)
            long endMask = RIGHT_END_MASKS[direction];
            for (int pos = 0; pos < 64; pos++) {
                long rayMask = 0;
                long bitBoard = startingBitBoards[pos];
                while ((endMask & bitBoard) == 0) {
                    rayMask |= bitBoard; // add curr ray pos to mask
                    bitBoard >>>= RAY_DIRECTIONS[direction]; // shift to next pos
                }
                rayMask |= bitBoard; // add pos on the edge
                rayMask ^= startingBitBoards[pos];
                rayResult[direction + 4][pos] = rayMask;
            }
        }
        return rayResult;
    }

    // only pass in sliding pieces
    public static long getSlidingAttackWithBlockers(int pos, long blockers, int piece) {
        if (!(1 <= piece && piece <= 3)) throw new RuntimeException("Invalid piece");
        long slidingAttackMask = 0;
        // if isRook, only select even directions, else (bishop) odd
        boolean rankPiece = piece == 1 || piece == 2; // every sliding piece but bishop
        boolean diagonalPiece = piece != 2; // not a rook
        for (int direction = rankPiece ? 0 : 1; direction < 8; direction += (rankPiece && diagonalPiece ? 1 : 2)) {
            long maskedBlocker = RAYS[direction][pos] & blockers; // find any piece in the way
            int closestBlockerPos = getPosOfLeastSigBit(maskedBlocker);
            if (closestBlockerPos == -1) { // no blocker in this direction
                slidingAttackMask |= RAYS[direction][pos];
            } else {
                long rayMask = RAYS[direction][closestBlockerPos];
                slidingAttackMask |= RAYS[direction][pos] ^ rayMask;
            }
        }
        return slidingAttackMask;
    }

    private long magicHash(int pos, long blockers, int index_bits, long magic) {
        return (blockers * magic) >>> (64 - index_bits);
    }
}
