package getzit.net.estimateit;

import java.util.Random;

public final class NumberGenerators {
    public static int nextIntTo(Random random, int low, int high) {
        return random.nextInt(high - low) + low;
    }

    public static int nextIntTo(Random random, int low, int high, RandomGenerator<Double> distribution) {
        return ((int) (distribution.generate(random) * (high - low))) + low;
    }

    public static RandomGenerator<Integer> intTo(int low, int high) {
        return r -> nextIntTo(r, low, high);
    }

    public static RandomGenerator<Integer> intTo(int low, int high, RandomGenerator<Double> distribution) {
        return r -> nextIntTo(r, low, high, distribution);
    }

    public static int nextIntThru(Random random, int low, int high) {
        return nextIntTo(random, low, high + 1);
    }

    public static int nextIntThru(Random random, int low, int high, RandomGenerator<Double> distribution) {
        return nextIntTo(random, low, high + 1, distribution);
    }

    public static RandomGenerator<Integer> intThru(int low, int high) {
        return r -> nextIntThru(r, low, high);
    }

    public static RandomGenerator<Integer> intThru(int low, int high, RandomGenerator<Double> distribution) {
        return r -> nextIntThru(r, low, high, distribution);
    }

    public static double nextDblFromScaleAndPrecision(
            Random random, RandomGenerator<Integer> scaleGenerator, RandomGenerator<Integer> precisionGenerator) {
        int scale = scaleGenerator.generate(random);
        int precision = precisionGenerator.generate(random);
        double minimum = Math.pow(10, scale - precision);
        return nextIntTo(random, 1, (int) Math.pow(10, precision)) * minimum;
    }

    public static double nextDblFromScaleAndPrecision(
            Random random, RandomGenerator<Integer> scaleGenerator, RandomGenerator<Integer> precisionGenerator,
            RandomGenerator<Double> distribution) {
        int scale = scaleGenerator.generate(random);
        int precision = precisionGenerator.generate(random);
        double minimum = Math.pow(10, scale - precision);
        return nextIntTo(random, 1, (int) Math.pow(10, precision), distribution) * minimum;
    }

    public static RandomGenerator<Double> dblFromScaleAndPrecision(
            RandomGenerator<Integer> scaleGenerator, RandomGenerator<Integer> precisionGenerator) {
        return random -> nextDblFromScaleAndPrecision(random, scaleGenerator, precisionGenerator);
    }

    public static RandomGenerator<Double> dblFromScaleAndPrecision(
            RandomGenerator<Integer> scaleGenerator, RandomGenerator<Integer> precisionGenerator,
            RandomGenerator<Double> distribution) {
        return random -> nextDblFromScaleAndPrecision(random, scaleGenerator, precisionGenerator, distribution);
    }
}
