package com.etherblood.connect4.solver;

public class PrimeUtil {

    public static void main(String[] args) {
        long start = 1L << 30;
        long next = start;
        for (int i = 0; i < 20; i++) {
            next = primeLessOrEqual(next);
            System.out.println(next);
            next--;
        }
    }

    public static long primeLessOrEqual(long n) {
        while (!isPrime(n)) {
            n--;
        }
        return n;
    }

    public static boolean isPrime(long n) {
        if (n <= 1) {
            return false;
        } else if (n <= 3) {
            return true;
        } else if ((n % 2 == 0) || (n % 3 == 0)) {
            return false;
        }
        for (int i = 5; i * i <= n; i += 6) {
            if ((n % i == 0) || (n % (i + 2) == 0)) {
                return false;
            }
        }
        return true;
    }
}
