package getzit.net.estimateit;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
            RandomGenerator<Double> dbl = NumberGenerators.dblFromScaleAndPrecision(
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
                RandomGenerator<Double> dbl = NumberGenerators.dblFromScaleAndPrecision(
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
        when(scaleGenerator.generate(Mockito.any())).thenReturn(1);
        when(precisionGenerator.generate(Mockito.any())).thenReturn(2);
        NumberGenerators.nextDblFromScaleAndPrecision(random, scaleGenerator, precisionGenerator);
        verify(scaleGenerator).generate(random);
        verify(precisionGenerator).generate(random);
    }

    void testIntToFitsRange(Random random, RandomGenerator<Integer> generator, int low, int high) {
        int tries = high - low;
        tries = tries * tries * 10;
        int minFound = Integer.MAX_VALUE, maxFound = Integer.MIN_VALUE;
        for (int i = 0; i < tries; i++) {
            int value = generator.generate(random);
            assertTrue(value + " outside of (" + low + ", " + high + ")",
                    low <= value && value < high);
            minFound = Math.min(minFound, value);
            maxFound = Math.max(maxFound, value);
        }
        assertEquals(minFound, low);
        assertEquals(maxFound, high - 1);
    }

    @Test
    public void testIntToFitsRange() {
        Random random = new Random(0xc8368f3026840274L);
        testIntToFitsRange(random, NumberGenerators.intTo(3, 17), 3, 17);
    }

    @Test
    public void testIntToWithUnitSquareDistributionFitsRange() {
        Random random = new Random(0xfd859401db2451e4L);
        testIntToFitsRange(random, NumberGenerators.intTo(3, 17, Random::nextDouble), 3, 17);
    }

    @Test
    public void testIntToWithDistribution() {
        assertEquals(4, NumberGenerators.nextIntTo(mock(Random.class), 1, 5, r -> 0.75));
    }
}
