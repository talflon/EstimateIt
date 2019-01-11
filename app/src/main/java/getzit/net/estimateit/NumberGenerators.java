package getzit.net.estimateit;

import java.lang.Math;

public final class NumberGenerators {
    public static RandomGenerator<Integer> integerRange(int low, int high) {
        return random -> low + random.nextInt(high - low);
    }

    public static RandomGenerator<Double> dbl(final double low, final double high, final double precision) {
        return random -> Math.round((low + random.nextDouble() * (high - low)) / precision) * precision;
    }

    public static RandomGenerator<Double> dbl(final double radius, final double precision) {
        return random -> Math.round(((random.nextInt(2) * 2 - 1) * random.nextDouble() * radius) * precision) / precision;
    }
}
