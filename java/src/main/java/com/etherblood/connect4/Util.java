package com.etherblood.connect4;

/**
 *
 * @author Philipp
 */
public class Util {

    public static String humanReadableNanos(long nanos) {
        int count = 0;
        while (nanos > 10000 && count < 3) {
            nanos /= 1000;
            count++;
        }
        if (count == 3) {
            return nanos + "s";
        }
        return nanos + ("nµm".charAt(count) + "") + "s";
    }

    public static long toLongFlag(int value) {
        return 1L << value;
    }

    public static long toLongMask(int value) {
        return toLongFlag(value) - 1;
    }

    public static int toIntFlag(int value) {
        return 1 << value;
    }

    public static int toIntMask(int value) {
        return toIntFlag(value) - 1;
    }

    public static int ceilDiv(int x, int y) {
        return -Math.floorDiv(-x, y);
    }

    public static int floorLog(long mask) {
        return 63 - Long.numberOfLeadingZeros(mask);
    }

    public static int ceilLog(long mask) {
        return 64 - Long.numberOfLeadingZeros(mask - 1);
    }
}
