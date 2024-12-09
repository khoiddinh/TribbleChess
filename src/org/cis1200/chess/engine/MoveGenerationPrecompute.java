package org.cis1200.chess.engine;

import java.util.ArrayList;
import java.util.Random;

import static org.cis1200.chess.engine.BitBoardFunctions.getPosOfLeastSigBit;
import static org.cis1200.chess.engine.BitBoardFunctions.getPosOfMostSigBit;

public class MoveGenerationPrecompute {

    public static final long LEFT_MASK = 0x8080808080808080L;
    public static final long RIGHT_MASK = 0x101010101010101L;
    public static final long TOP_MASK = 0xFF00000000000000L;
    public static final long BOTTOM_MASK = 0xFFL;

    private static long[] startingBitBoards;

    private static long[] kingAttackMasks;
    private static long[] knightAttackMasks;

    private static long[] whitePawnMoveMasks;
    private static long[] whitePawnAttackMasks;

    private static long[] blackPawnMoveMasks;
    private static long[] blackPawnAttackMasks;

    private static long[] rookAttackMasks;
    private static long[] bishopAttackMasks;
    private static long[] queenAttackMasks;

    // bitshift amounts corresponding to each direction
    // NOTE: left shift vs right shift
    public static final int[] RAY_DIRECTIONS = {1, 9, 8, 7};
    public static final long[] LEFT_END_MASKS =
        {LEFT_MASK, LEFT_MASK | TOP_MASK, TOP_MASK, RIGHT_MASK | TOP_MASK};
    public static final long[] RIGHT_END_MASKS =
        {RIGHT_MASK, RIGHT_MASK | BOTTOM_MASK, BOTTOM_MASK, LEFT_MASK | BOTTOM_MASK};

    private static long[][] rays; // [direction][position]
    // left = 0, up left = 1, up = 2, up right = 3 (left shifts)
    // right = 4, down right = 5, down = 6, down left = 7 (right shifts)
    // position is square (n) where 0 <= n < 64

    // Magic Bitboard Lookup Tables
    private static long[][] bishopAttackTable; // [pos][has
    private static long[][] rookAttackTable;

    private static long[] bishopMagics;
    private static long[] rookMagics;

    public MoveGenerationPrecompute() {
        startingBitBoards = new long[64];
        for (int i = 0; i < 64; i++) {
            startingBitBoards[i] = 0x1L << 63 - i;
        }
        System.out.print("Generating Precomputation Tables... ");
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

        rays = generateRays();

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
        rookMagics = new long[64];
        bishopMagics = new long[64];
        System.out.println("Done!");

        rookAttackTable = new long[64][]; // 64 * 2^12 (4096) (possible combos of blocker)
        bishopAttackTable = new long[64][]; // 64 * 2^9 (512)

        generateRookMagicBitBoards();
        generateBishopMagicBitBoards();
    }

    public static long[][] getRays() {
        return rays;
    }
    public static long[] getStartingBitBoards() {
        return startingBitBoards;
    }
    public static long[] getKingAttackMasks() {
        return kingAttackMasks;
    }

    public static long[] getKnightAttackMasks() {
        return knightAttackMasks;
    }

    public static long[] getWhitePawnMoveMasks() {
        return whitePawnMoveMasks;
    }

    public static long[] getWhitePawnAttackMasks() {
        return whitePawnAttackMasks;
    }

    public static long[] getBlackPawnMoveMasks() {
        return blackPawnMoveMasks;
    }

    public static long[] getBlackPawnAttackMasks() {
        return blackPawnAttackMasks;
    }

    public static long[] getQueenAttackMasks() {
        return queenAttackMasks;
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
        long bottomRank = BOTTOM_MASK;
        long top2Ranks = 0xFFFF000000000000L; // Exclude ranks 7 and 8
        long bottom2Ranks = 0xFFFFL; // Exclude ranks 1 and 2

        // Knight moves: 8 directions
        // Up moves (ranks increasing)
        if ((bitBoard & (fileA | top2Ranks)) == 0) {
            attackMask |= (bitBoard << 17); // up 2, left 1
        }
        if ((bitBoard & (fileAB | topRank)) == 0) {
            attackMask |= (bitBoard << 10); // up 1, left 2
        }
        if ((bitBoard & (fileH | top2Ranks)) == 0) {
            attackMask |= (bitBoard << 15); // up 2, right 1
        }
        if ((bitBoard & (fileGH | topRank)) == 0) {
            attackMask |= (bitBoard << 6); // up 1, right 2
        }

        // Down moves (ranks decreasing)
        if ((bitBoard & (fileA | bottom2Ranks)) == 0) {
            attackMask |= (bitBoard >>> 15); // down 2, left 1
        }
        if ((bitBoard & (fileAB | bottomRank)) == 0) {
            attackMask |= (bitBoard >>> 6); // down 1, left 2
        }
        if ((bitBoard & (fileH | bottom2Ranks)) == 0) {
            attackMask |= (bitBoard >>> 17); // down 2, right 1
        }
        if ((bitBoard & (fileGH | bottomRank)) == 0) {
            attackMask |= (bitBoard >>> 10); // down 1, right 2
        }

        return attackMask;
    }

    public long generateWhitePawnMoveMask(int square) {
        long bitBoard = startingBitBoards[square];
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
        if ((bitBoard & (BOTTOM_MASK | TOP_MASK)) != 0) {
            return bitBoard; // if at final row, promote, no valid moves
        }
        long mask = 0;
        if ((bitBoard & LEFT_MASK) == 0) mask |= bitBoard >>> 7; // if not on left edge
        if ((bitBoard & RIGHT_MASK) == 0) mask |= bitBoard >>> 9; // if not on right edge
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

        // remove edge for magic bitboard
        // attackMask &= ~EDGE_MASK;
        return attackMask;
    }

    public long generateBishopAttackMask(int square) {
        long attackMask = 0;
        for (int direction = 1; direction < 8; direction += 2) {
            attackMask |= rays[direction][square];
        }

        // remove edge for magic bitboard
        //attackMask &= ~EDGE_MASK;
        return attackMask;
    }

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
                rayMask ^= startingBitBoards[pos];
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
        if (!(1 <= piece && piece <= 3)) {
            throw new RuntimeException("Invalid piece");
        }
        long slidingAttackMask = 0;
        // if isRook, only select even directions, else (bishop) odd
        boolean rankPiece = piece == 1 || piece == 2; // every sliding piece but bishop
        boolean diagonalPiece = piece != 2; // not a rook
        if ((startingBitBoards[pos] & blockers) != 0) { // if the blocker includes piece
            blockers ^= startingBitBoards[pos];
        }
        for (int direction = rankPiece ? 0 : 1; direction < 8; direction += (rankPiece && diagonalPiece ? 1 : 2)) {
            long maskedBlocker = rays[direction][pos] & blockers; // find any piece in the way
            int closestBlockerPos;
            if (direction >= 4) { // if right or down direction
                closestBlockerPos = getPosOfMostSigBit(maskedBlocker);
            } else {
                closestBlockerPos = getPosOfLeastSigBit(maskedBlocker);
            }
            if (closestBlockerPos == -1) { // no blocker in this direction
                slidingAttackMask |= rays[direction][pos];
            } else {
                long rayMask = rays[direction][closestBlockerPos];
                slidingAttackMask |= rays[direction][pos] ^ rayMask;
                // add blocker back to attack mask
                // if friendly, will filter out in getPossibleLegalMoves
                slidingAttackMask |= startingBitBoards[closestBlockerPos];
            }
        }
        return slidingAttackMask;
    }

    private long generateDenseLong() {
        Random random = new Random();
        long num1 = random.nextLong() & 0xFFFF;
        long num2 = random.nextLong() & 0xFFFF;
        long num3 = random.nextLong() & 0xFFFF;
        long num4 = random.nextLong() & 0xFFFF;
        return num1 | (num2 << 16) | (num3 << 32) | (num4 << 48);
    }

    private long generateMagicCandidate() {
        return generateDenseLong() & generateDenseLong() & generateDenseLong();
    }

    // carry-rippler
    private ArrayList<Long> getAllBlockerCombinations(int pos, int piece) {
        if (piece != 2 && piece != 3) throw new RuntimeException("Invalid piece");
        ArrayList<Long> blockerCombinations = new ArrayList<>();
        long subset = 0;
        long mask = (piece == 2) ?
                rookAttackMasks[pos] : bishopAttackMasks[pos]; // no blocker attack mask
        do {
            subset = (subset - mask) & mask;
            blockerCombinations.add(subset);
        } while (subset != 0);
        return blockerCombinations;
    }

    private long findRookMagicNumber(int pos, ArrayList<Long> blockerCombos) {
        long magic = 0;
        boolean foundMagic = false;
        long[] currMap = new long[16384];
        int shiftAmount = Long.bitCount(rookAttackMasks[pos]);
        while (!foundMagic) {
            magic = generateMagicCandidate();
            // clear previous incomplete attack table at pos
            currMap = new long[16384];
            boolean magicIsInvalid = false;
            for (long blocker : blockerCombos) {
                int index = magicHash(blocker, shiftAmount, magic);
                long attackMask = getSlidingAttackWithBlockers(pos, blocker, 2);
                // if already found an attack mask at index
                if (currMap[index] != 0) {
                    // if not same attackMask magic is invalid
                    if ((attackMask != currMap[index])) {
                        magicIsInvalid = true;
                        break;
                    }
                } else { // if no attack mask there yet, add one
                    currMap[index] = attackMask;
                }
            }
            if (!magicIsInvalid) {
                foundMagic = true;
            }
        }
        rookAttackTable[pos] = currMap;
        return magic;
    }

    // inits it directly
    public void generateRookMagicBitBoards() {
        System.out.print("Generating Rook Magic Hash Precomputation Tables... ");
        for (int pos = 0; pos < 64; pos++) {
            rookMagics[pos] =
                    findRookMagicNumber(pos, getAllBlockerCombinations(pos, 2));
        }
        System.out.println("Done!");

    }

    private long findBishopMagicNumber(int pos, ArrayList<Long> blockerCombos) {
        long magic = 0;
        boolean foundMagic = false;
        long[] currMap = new long[16384];
        int shiftAmount = Long.bitCount(bishopAttackMasks[pos]);
        while (!foundMagic) {
            magic = generateMagicCandidate();
            // clear previous incomplete attack table at pos
            currMap = new long[16384];
            boolean magicIsInvalid = false;
            for (long blocker : blockerCombos) {
                int index = magicHash(blocker, shiftAmount, magic);
                long attackMask = getSlidingAttackWithBlockers(pos, blocker, 3);
                // if already found an attack mask at index
                if (currMap[index] != 0) {
                    // if not same attackMask magic is invalid
                    if ((attackMask != currMap[index])) {
                        magicIsInvalid = true;
                        break;
                    }
                } else { // if no attack mask there yet, add one
                    currMap[index] = attackMask;
                }
            }
            if (!magicIsInvalid) {
                foundMagic = true;
            }
        }
        bishopAttackTable[pos] = currMap;
        return magic;
    }

    // inits it directly
    public void generateBishopMagicBitBoards() {
        System.out.print("Generating Bishop Magic Hash Precomputation Tables... ");
        for (int pos = 0; pos < 64; pos++) {
            bishopMagics[pos] = findBishopMagicNumber(pos, getAllBlockerCombinations(pos, 3));
        }
        System.out.println("Done!");
    }
    private int magicHash(long blockers, int shift, long magic){
        return (int) ((blockers * magic) >>> (64-shift));
    }

    // takes in generic blockers, not masked ones for the row and col (or diagonal)
    public long getSlidingMagicAttack(int pos, long blockers, int piece) {
        long slidingAttackMask = 0;
        switch (piece) {
            case 1: {// queen
                long rookMaskedBlockers = blockers & rookAttackMasks[pos];
                int rookIndex = magicHash(rookMaskedBlockers, Long.bitCount(rookAttackMasks[pos]), rookMagics[pos]);
                long bishopMaskedBlockers = blockers & bishopAttackMasks[pos];
                int bishopIndex = magicHash(bishopMaskedBlockers, Long.bitCount(bishopAttackMasks[pos]), bishopMagics[pos]);
                slidingAttackMask = rookAttackTable[pos][rookIndex] | bishopAttackTable[pos][bishopIndex];
                break;
            }
            case 2: { // rook
                long maskedBlockers = blockers & rookAttackMasks[pos];
                int index = magicHash(maskedBlockers, Long.bitCount(rookAttackMasks[pos]), rookMagics[pos]);
                slidingAttackMask = rookAttackTable[pos][index];
                break;
            }
            case 3: {  // bishop
                long maskedBlockers = blockers & bishopAttackMasks[pos];
                int index = magicHash(maskedBlockers, Long.bitCount(bishopAttackMasks[pos]), bishopMagics[pos]);
                slidingAttackMask = bishopAttackTable[pos][index];
                break;
            }
            default:
                throw new RuntimeException();
        }
        return slidingAttackMask;
    }
}
