package org.cis1200.test.engine;

import org.cis1200.chess.engine.MoveGenerationPrecompute;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoveGenerationPrecomputeTest {
    public static final long LEFT_MASK = 0x8080808080808080L;
    public static final long RIGHT_MASK = 0x101010101010101L;
    public static final long TOP_MASK = 0xFF00000000000000L;
    public static final long BOTTOM_MASK = 0xFFL;
    MoveGenerationPrecompute tables = new MoveGenerationPrecompute();
    public void printBitBoard(long val) {
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
    @Test
    void testGenerateRookAttackMask() {
        MoveGenerationPrecompute chessAttacks = new MoveGenerationPrecompute();

        // Test rook on a8 (index 0, top-left indexing)
        long actualMask = chessAttacks.generateRookAttackMask(0);
        long expectedMask = LEFT_MASK ^ TOP_MASK; // Expected bitboard for a8
        assertEquals(expectedMask, actualMask, "Rook attack mask for a8 is incorrect.");

        // Test rook on f5 (index 29, top-left indexing)
        actualMask = chessAttacks.generateRookAttackMask(29);
        expectedMask = 0x40404FB04040404L; // Expected bitboard for f5
        assertEquals(expectedMask, actualMask, "Rook attack mask for f5 is incorrect.");

        // Test rook on h1 (index 7, top-left indexing)
        actualMask = chessAttacks.generateRookAttackMask(63);
        expectedMask = 0x01010101010101FEL; // Expected bitboard for h8
        assertEquals(expectedMask, actualMask, "Rook attack mask for h8 is incorrect.");
    }
    @Test
    void testRaysLeft() {
        long actualRay = MoveGenerationPrecompute.RAYS[0][0];
        printBitBoard(actualRay);
        long expectedRay = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
        assertEquals(expectedRay, actualRay, "Left ray for 0 is incorrect.");
    }

    @Test
    void testRaysUpLeft() {
        long actualRay = MoveGenerationPrecompute.RAYS[1][20];
        printBitBoard(actualRay);
        long expectedRay = 0b00100000_00010000_00000000_00000000_00000000_00000000_00000000_00000000L;
        assertEquals(expectedRay, actualRay, "Up-left ray for 20 is incorrect.");
    }

    @Test
    void testRaysUp() {
        long actualRay = MoveGenerationPrecompute.RAYS[2][35];
        long expectedRay = 0b00010000_00010000_00010000_00010000_00000000_00000000_00000000_00000000L;
        assertEquals(expectedRay, actualRay, "Up ray for 35 is incorrect.");
    }

    @Test
    public void testRaysUpRight() {
        long actualRay = MoveGenerationPrecompute.RAYS[3][27];
        long expectedRay = 0b00000010_00000100_00001000_00000000_00000000_00000000_00000000_00000000L;
        assertEquals(expectedRay, actualRay, "Up-right ray for 27 is incorrect.");
    }

    @Test
    public void testRaysRight() {
        long actualRay = MoveGenerationPrecompute.RAYS[4][24];
        long expectedRay = 0b00000000_00000000_00000000_01111111_00000000_00000000_00000000_00000000L;
        assertEquals(expectedRay, actualRay, "Right ray for 24 is incorrect.");
    }

    @Test
    public void testRaysDownRight() {
        long actualRay = MoveGenerationPrecompute.RAYS[5][45];
        long expectedRay = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000010_00000001L;
        assertEquals(expectedRay, actualRay, "Down-right ray for 45 is incorrect.");
    }

    @Test
    public void testRaysDown() {
        long actualRay = MoveGenerationPrecompute.RAYS[6][19];
        long expectedRay = 0b00000000_00000000_00000000_00010000_00010000_00010000_00010000_00010000L;
        assertEquals(expectedRay, actualRay, "Down ray for 19 is incorrect.");
    }

    @Test
    public void testRaysDownLeft() {
        long actualRay = MoveGenerationPrecompute.RAYS[7][15];
        long expectedRay = 0b00000000_00000000_00000010_00000100_00001000_00010000_00100000_01000000L;
        assertEquals(expectedRay, actualRay, "Down-left ray for 15 is incorrect.");
    }

    @Test
    public void testKnightMask() {
        long actualMask = MoveGenerationPrecompute.knightAttackMasks[0];
        long expectedMask = 0b00000000_00100000_01000000_00000000_00000000_00000000_00000000_00000000L;
        assertEquals(expectedMask, actualMask, "Knight mask for pos 0 is incorrect.");


        actualMask = MoveGenerationPrecompute.knightAttackMasks[7];
        expectedMask = 0b00000000_00000100_00000010_00000000_00000000_00000000_00000000_00000000L;
        assertEquals(expectedMask, actualMask, "Knight mask for pos 7 is incorrect.");

        actualMask = MoveGenerationPrecompute.knightAttackMasks[56];
        expectedMask = 0b00000000_00000000_00000000_00000000_00000000_01000000_00100000_00000000L;
        assertEquals(expectedMask, actualMask, "Knight mask for pos 55 is incorrect.");

        actualMask = MoveGenerationPrecompute.knightAttackMasks[63];
        expectedMask = 0b00000000_00000000_00000000_00000000_00000000_00000010_00000100_00000000L;
        assertEquals(expectedMask, actualMask, "Knight mask for pos 63 is incorrect.");

        actualMask = MoveGenerationPrecompute.knightAttackMasks[27]; //d5
        expectedMask = 0b00000000_00101000_01000100_00000000_01000100_00101000_00000000_00000000L;
        assertEquals(expectedMask, actualMask, "Knight mask for pos 63 is incorrect.");

    }

    @Test
    public void testPawnMask() {
        long actualMask = MoveGenerationPrecompute.whitePawnMoveMasks[48];
        long expectedMask = 0b00000000_00000000_00000000_00000000_10000000_10000000_00000000_00000000L;
        assertEquals(expectedMask, actualMask, "Pawn mask for pos 48 is incorrect.");

        actualMask = MoveGenerationPrecompute.whitePawnAttackMasks[55];
        expectedMask = 0b00000000_00000000_00000000_00000000_00000000_00000010_00000000_00000000L;
        assertEquals(expectedMask, actualMask, "Pawn mask for pos 55 is incorrect.");

        actualMask = MoveGenerationPrecompute.whitePawnAttackMasks[54];
        expectedMask = 0b00000000_00000000_00000000_00000000_00000000_00000101_00000000_00000000L;
        assertEquals(expectedMask, actualMask, "Pawn mask for pos 55 is incorrect.");

        actualMask = MoveGenerationPrecompute.blackPawnMoveMasks[8];
        expectedMask = 0b00000000_00000000_10000000_10000000_00000000_00000000_00000000_00000000L;
        assertEquals(expectedMask, actualMask, "Pawn mask for pos 8 is incorrect.");

        actualMask = MoveGenerationPrecompute.blackPawnAttackMasks[9];
        expectedMask = 0b00000000_00000000_10100000_00000000_00000000_00000000_00000000_00000000L;
        assertEquals(expectedMask, actualMask, "Pawn mask for pos 9 is incorrect.");
    }
}
