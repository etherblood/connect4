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

    //https://stackoverflow.com/a/3758880
    public static String humanReadableByteCountBin(long bytes) {
        long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        return b < 1024L ? bytes + " B"
                : b <= 0xfffccccccccccccL >> 40 ? String.format("%.1f KiB", bytes / 0x1p10)
                        : b <= 0xfffccccccccccccL >> 30 ? String.format("%.1f MiB", bytes / 0x1p20)
                                : b <= 0xfffccccccccccccL >> 20 ? String.format("%.1f GiB", bytes / 0x1p30)
                                        : b <= 0xfffccccccccccccL >> 10 ? String.format("%.1f TiB", bytes / 0x1p40)
                                                : b <= 0xfffccccccccccccL ? String.format("%.1f PiB", (bytes >> 10) / 0x1p40)
                                                        : String.format("%.1f EiB", (bytes >> 20) / 0x1p40);
    }

    public static String toPercentage(long a, long b, int digits) {
        return toPercentage((double) a / b, digits);
    }

    public static String toPercentage(double n, int digits) {
        return String.format("%." + digits + "f", n * 100) + "%";
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
