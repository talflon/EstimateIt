package getzit.net.estimateit;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class NumberGeneratorsUnitTest {
    @Test
    public void testDbl() {
        Random random = new Random("for a random seed".hashCode());
        for (int i = 0; i < 1000; i++) {
            double precision = Math.pow(10, random.nextInt(5) - 2);
            double low = precision * random.nextInt(100);
            double high = low + precision * random.nextInt(1000);
            RandomGenerator<Double> dbl = NumberGenerators.dbl(low, high, precision);
            for (int k = 0; k < 100; k++) {
                double value = dbl.generate(random);
                assertEquals(high, Math.max(high, value), 1e-9);
                assertEquals(low, Math.min(low, value), 1e-9);
                double precisionError = (value / precision) % 1;
                if (precisionError > 0.5) {
                    precisionError -= 1.0;
                }
                assertEquals(0.0, precisionError, 1e-9);
            }
        }
    }
}
