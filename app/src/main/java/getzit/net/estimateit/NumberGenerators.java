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

    public static RandomGenerator<Double> dblFromScaleAndPrecision(
            RandomGenerator<Integer> scaleGenerator, RandomGenerator<Integer> precisionGenerator) {
        return random -> {
            int scale = scaleGenerator.generate(random);
            int precision = precisionGenerator.generate(random);
            double minimum = Math.pow(10, scale - precision);
            return intTo(random, 1, (int) Math.pow(10, precision)) * minimum;
        };
    }
}
