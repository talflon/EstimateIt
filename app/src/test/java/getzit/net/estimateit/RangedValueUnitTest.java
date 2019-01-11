package getzit.net.estimateit;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RangedValueUnitTest {
    private static final double[] POSITIVE_NUMBERS = {
            12, 0, 3, 123.456, 1.1113
    };
    private static final String[] INVALID_STRINGS = {
            "",
            "±3",
            "±0.5",
            "1.2.3",
            "7±2.99.1",
            "6+1",
            "10.3-2",
            "9.9+0.1",
            "three",
            "7±two",
    };

    @Test
    public void eitherOrderWorks() {
        assertEquals(new RangedValue(3, 12), new RangedValue(12, 3));
        assertEquals(new RangedValue(-100, -5), new RangedValue(-5, -100));
        assertEquals(new RangedValue(0, 1), new RangedValue(1, 0));
        assertEquals(new RangedValue(-8, 0), new RangedValue(0, -8));
    }

    static double randomDouble(Random random, double low, double high) {
        return low + (high - low) * random.nextDouble();
    }

    static double randomDouble(Random random, double high) {
        return randomDouble(random, -high, high);
    }

    static double randomDouble(Random random, RangedValue range) {
        return randomDouble(random, range.getLowerValue(), range.getUpperValue());
    }

    static RangedValue randomRange(Random random, double low, double high) {
        return new RangedValue(randomDouble(random, low, high), randomDouble(random, low, high));
    }

    static RangedValue randomRange(Random random, double high) {
        return new RangedValue(randomDouble(random, high), randomDouble(random, high));
    }

    static RangedValue randomRange(Random random, RangedValue range) {
        return new RangedValue(randomDouble(random, range), randomDouble(random, range));
    }

    static RangedValue randomRange(Random random) {
        return randomRange(random, 1e4);
    }

    @Test
    public void coversInside() {
        final Random random = new Random("InSiDe".hashCode());
        double[] values = new double[4];
        for (int i = 0; i < 100; i++) {
            for (int k = 0; k < values.length; k++) {
                values[k] = randomDouble(random, 1e4);
            }
            Arrays.sort(values);
            RangedValue outer = new RangedValue(values[0], values[3]);
            RangedValue inner = new RangedValue(values[1], values[2]);
            assertTrue(outer.covers(inner));
            if (!outer.equals(inner)) {
                assertFalse(inner.covers(outer));
            }
        }
    }

    @Test
    public void overlappingDoesntCover() {
        final Random random = new Random("overLAP".hashCode());
        double[] values = new double[4];
        for (int i = 0; i < 100; i++) {
            for (int k = 0; k < values.length; k++) {
                values[k] = randomDouble(random, 1e4);
            }
            Arrays.sort(values);
            RangedValue rv1 = new RangedValue(values[0], values[2]);
            RangedValue rv2 = new RangedValue(values[1], values[3]);
            assertFalse(rv1.covers(rv2));
            assertFalse(rv2.covers(rv1));
        }
    }

    @Test
    public void disjointDoesntCover() {
        final Random random = new Random("D1sjOInT".hashCode());
        double[] values = new double[4];
        for (int i = 0; i < 100; i++) {
            for (int k = 0; k < values.length; k++) {
                values[k] = randomDouble(random, 1e4);
            }
            Arrays.sort(values);
            RangedValue rv1 = new RangedValue(values[0], values[1]);
            RangedValue rv2 = new RangedValue(values[2], values[3]);
            assertFalse(rv1.covers(rv2));
            assertFalse(rv2.covers(rv1));
        }
    }

    @Test
    public void coversSelf() {
        final Random random = new Random(0x268f685debc3b352L);
        for (int i = 0; i < 100; i++) {
            RangedValue range = randomRange(random);
            assertTrue(range.covers(range));
        }
    }

    @Test
    public void equalsSelf() {
        final Random random = new Random(0x21c13357830d95b0L);
        for (int i = 0; i < 100; i++) {
            RangedValue range = randomRange(random);
            assertEquals(range, range);
        }
    }

    @Test
    public void parsesSingleNumbers() {
        for (double value : POSITIVE_NUMBERS) {
            assertEquals(RangedValue.forExact(value), RangedValue.parse(Double.toString(value)));
        }
    }

    @Test
    public void parsesNegativeNumbers() {
        for (double value : POSITIVE_NUMBERS) {
            if (value == 0) continue;
            assertEquals(RangedValue.forExact(-value), RangedValue.parse('–' + Double.toString(value)));
        }
    }

    @Test
    public void parsesBounds() {
        for (double value : POSITIVE_NUMBERS) {
            for (double radius : POSITIVE_NUMBERS) {
                assertEquals(RangedValue.forBounds(value, radius),
                        RangedValue.parse(Double.toString(value) + '±' + Double.toString(radius)));
            }
        }
    }

    @Test
    public void parsesBoundsOnNegative() {
        for (double value : POSITIVE_NUMBERS) {
            for (double radius : POSITIVE_NUMBERS) {
                assertEquals(RangedValue.forBounds(-value, radius),
                        RangedValue.parse('-' + Double.toString(value) + '±' + Double.toString(radius)));
            }
        }
    }

    @Test
    public void invalidThrowsException() {
        for (String invalid : INVALID_STRINGS) {
            try {
                RangedValue.parse(invalid);
                fail("Expected an IllegalArgumentException for \"" + invalid + '"');
            } catch (IllegalArgumentException e) {
                /* pass */
            }
        }
    }

    @Test
    public void testAdd() {
        final Random random = new Random(0x9e636c9c89d69672L);
        for (int i = 0; i < 100; i++) {
            RangedValue r1 = randomRange(random);
            RangedValue r2 = randomRange(random);
            RangedValue sum = r1.add(r2);
            for (int k = 0; k < 10; k++) {
                assertTrue(sum.covers(RangedValue.forExact(randomDouble(random, r1) + randomDouble(random, r2))));
            }
        }
    }

    @Test
    public void testSubtract() {
        final Random random = new Random(0x61dc055701d242adL);
        for (int i = 0; i < 100; i++) {
            RangedValue r1 = randomRange(random);
            RangedValue r2 = randomRange(random);
            RangedValue sum = r1.subtract(r2);
            for (int k = 0; k < 10; k++) {
                assertTrue(sum.covers(RangedValue.forExact(randomDouble(random, r1) - randomDouble(random, r2))));
            }
        }
    }

    @Test
    public void testMultiply() {
        final Random random = new Random(0x4eaf5b8844eacd23L);
        for (int i = 0; i < 100; i++) {
            RangedValue r1 = randomRange(random);
            RangedValue r2 = randomRange(random);
            RangedValue sum = r1.multiply(r2);
            for (int k = 0; k < 10; k++) {
                assertTrue(sum.covers(RangedValue.forExact(randomDouble(random, r1) * randomDouble(random, r2))));
            }
        }
    }
}
