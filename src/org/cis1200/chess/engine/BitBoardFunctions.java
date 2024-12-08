package org.cis1200.chess.engine;

public class BitBoardFunctions {
    public BitBoardFunctions() {
    }

    public static int getPosOfMostSigBit(long n) {
        if (n == 0) return -1;
        return Long.numberOfLeadingZeros(n);
    }
    // gets the top left zero indexed position of least sig bit
    public static int getPosOfLeastSigBit(long n) {
        if (n == 0) return -1;
        return 63-Long.numberOfTrailingZeros(n);
    }
    // or operators all bitboards in array together
    public static long orBitBoardArray(long[] bitBoards) {
        long result = 0;
        for (long bitBoard : bitBoards) {
            result |= bitBoard;
        }
        return result;
    }

    public static void printBinary(int n) {
        System.out.println(Integer.toBinaryString(n));
    }
}
