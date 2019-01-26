package getzit.net.estimateit;

import java.util.Random;

public final class NumberGenerators {
    public static int intTo(Random random, int low, int high) {
        return random.nextInt(high - low) + low;
    }

    public static RandomGenerator<Integer> intTo(int low, int high) {
        return r -> intTo(r, low, high);
    }

    public static int intThru(Random random, int low, int high) {
        return intTo(random, low, high + 1);
    }

    public static RandomGenerator<Integer> intThru(int low, int high) {
        return r -> intThru(r, low, high);
    }
}
