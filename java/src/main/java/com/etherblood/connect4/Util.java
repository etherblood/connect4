package com.etherblood.connect4;

/**
 *
 * @author Philipp
 */
public class Util {

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

    }
}
