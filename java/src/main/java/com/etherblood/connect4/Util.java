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

    public static class Long {

        public static long toFlag(int value) {
            return 1L << value;
        }

        public static long toMask(int value) {
            return toFlag(value) - 1;
        }

    }

    public static class Int {

        public static int toFlag(int value) {
            return 1 << value;
        }

        public static int toMask(int value) {
            return toFlag(value) - 1;
        }

        public static int ceilDiv(int x, int y) {
            return -Math.floorDiv(-x, y);
        }

    }
}
