package org.cis1200.chess.engine;

public class BitBoardFunctions {
    public BitBoardFunctions() {
    }

    public static int getPosOfMostSigBit(long n) {
        if (n == 0) {
            return - 1;
        }
        return Long.numberOfLeadingZeros(n);
    }
    // gets the top left zero indexed position of least sig bit
    public static int getPosOfLeastSigBit(long n) {
        if (n == 0) {
            return - 1;
        }
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
}
