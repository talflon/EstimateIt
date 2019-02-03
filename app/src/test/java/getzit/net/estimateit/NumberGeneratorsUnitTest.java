package getzit.net.estimateit;

import org.junit.Test;

import java.util.Random;

import static getzit.net.estimateit.NumberGenerators.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NumberGeneratorsUnitTest {
    @Test
    public void testDblFromScaleAndPrecisionConforms() {
        Random random = new Random(0xcff5618ae7ea359L);
        for (int i = 0; i < 1000; i++) {
            int scale = random.nextInt(10) - 4;
            int precision = random.nextInt(5) + 1;
            RandomGenerator<Double> dbl = dblFromScaleAndPrecision(
                    RandomGenerator.exactly(scale), RandomGenerator.exactly(precision));
            for (int k = 0; k < 100; k++) {
                double value = dbl.generate(random);
                int valueScale = (int) Math.ceil(Math.log10(value));
                assertTrue(
                        String.format("Scale %d of %f doesn't match desired scale %d with precision %d",
                                valueScale, value, scale, precision),
                        scale - precision <= valueScale && valueScale <= scale);
                double precisionError = (value * Math.pow(10, precision - valueScale)) % 1;
                if (precisionError > 0.5) {
                    precisionError -= 1.0;
                }
                assertEquals(String.format("Precision of %f is not %d", value, precision),
                        0.0, precisionError, 1e-9);
            }
        }
    }

    @Test
    public void testDblFromScaleAndPrecisionFillsScale() {
        Random random = new Random(0xf58b21a07a346f79L);
        for (int scale = -3; scale <= 4; scale++) {
            for (int precision = 1; precision <= 4; precision++) {
                RandomGenerator<Double> dbl = dblFromScaleAndPrecision(
                        RandomGenerator.exactly(scale), RandomGenerator.exactly(precision));
                double highTarget = 8 * Math.pow(10, scale - 1);
                double lowTarget = 2 * Math.pow(10, scale - precision);
                boolean foundHighTarget = false, foundLowTarget = false;
                long tries = (long) Math.pow(10, precision * 2 + 1);
                for (long k = 0; k < tries && !(foundHighTarget && foundLowTarget); k++) {
                    double value = dbl.generate(random);
                    if (value > highTarget) {
                        foundHighTarget = true;
                    } else if (value < lowTarget) {
                        foundLowTarget = true;
                    }
                }
                assertTrue(String.format("Didn't find anything above %f for scale %d and precision %d",
                        highTarget, scale, precision),
                        foundHighTarget);
                assertTrue(String.format("Didn't find anything below %f for scale %d and precision %d",
                        lowTarget, scale, precision),
                        foundLowTarget);
            }
        }
    }

    @Test
    public void testDblFromScaleAndPrecisionCallsGenerators() {
        Random random = new Random(0x7c854e664b4c27f5L);
        RandomGenerator<Integer> scaleGenerator = mock(RandomGenerator.class);
        RandomGenerator<Integer> precisionGenerator = mock(RandomGenerator.class);
        when(scaleGenerator.generate(any())).thenReturn(1);
        when(precisionGenerator.generate(any())).thenReturn(2);
        nextDblFromScaleAndPrecision(random, scaleGenerator, precisionGenerator);
        verify(scaleGenerator).generate(random);
        verify(precisionGenerator).generate(random);
    }

    private static void assertBetween(int value, int low, int high) {
        assertTrue(value + " outside of (" + low + ", " + high + ")",
                low <= value && value < high);
    }

    void testIntToFitsRange(Random random, RandomGenerator<Integer> generator, int low, int high) {
        int tries = high - low;
        tries = tries * tries * 10;
        int minFound = Integer.MAX_VALUE, maxFound = Integer.MIN_VALUE;
        for (int i = 0; i < tries; i++) {
            int value = generator.generate(random);
            assertBetween(value, low, high);
            minFound = Math.min(minFound, value);
            maxFound = Math.max(maxFound, value);
        }
        assertEquals(minFound, low);
        assertEquals(maxFound, high - 1);
    }

    @Test
    public void testIntToFitsRange() {
        Random random = new Random(0xc8368f3026840274L);
        testIntToFitsRange(random, intTo(3, 17), 3, 17);
    }

    @Test
    public void testIntToWithUnitSquareDistributionFitsRange() {
        Random random = new Random(0xfd859401db2451e4L);
        testIntToFitsRange(random, intTo(3, 17, Random::nextDouble), 3, 17);
    }

    @Test
    public void testIntToWithDistribution() {
        assertEquals(4, nextIntTo(mock(Random.class), 1, 5, r -> 0.75));
    }

    private static class GeneratorOf<T> implements RandomGenerator<T> {
        private T[] values;
        private int index = 0;

        public GeneratorOf(T... values) {
            this.values = values;
        }

        @Override
        public T generate(Random random) {
            return values[index++];
        }
    }

    @Test(timeout = 1000)
    public void testIntToWithDistributionTooHighStaysInRange() {
        assertBetween(
                nextIntThru(mock(Random.class), -1, 8,
                        new GeneratorOf<>(3.0, 2.0, 1.0, 0.5)),
                -1, 8);
    }

    @Test(timeout = 1000)
    public void testIntToWithDistributionTooLowStaysInRange() {
        assertBetween(
                nextIntThru(mock(Random.class), -1, 8,
                        new GeneratorOf<>(-2.0, -1.0, 0.0)),
                -1, 8);
    }

    @Test(timeout = 2000)
    public void testIntToWithDistributionNoHangHigh() {
        try {
            nextIntTo(mock(Random.class), 0, 1, r -> 2.0);
        } catch (RuntimeException e) {
            /* pass -- just testing for hang */
        }
    }

    @Test(timeout = 2000)
    public void testIntToWithDistributionNoHangLow() {
        try {
            nextIntTo(mock(Random.class), 0, 1, r -> -0.5);
        } catch (RuntimeException e) {
            /* pass -- just testing for hang */
        }
    }
}
