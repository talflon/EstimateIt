package getzit.net.estimateit;

import org.junit.Test;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RangedValueUnitTest {
    private static final double[] POSITIVE_NUMBERS = {
            12, 3, 123.456, 1.1113, 7.1, 234834, 234895.6, 2349345.12374, 0.000213, 0.01,
    };
    private static final String[] ZERO_REPS = {
            "0", "0.0", "0.00",
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
            ".",
            ".±.",
            "12±.",
    };
    private static final String[] MINUS_CHARS = { "-", "–" };

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

    static final DecimalFormat decimalFormat = new DecimalFormat("0.0###########");

    static class ParseExample {
        final RangedValue value;
        final String asString;

        ParseExample(RangedValue value, String asString) {
            this.value = value;
            this.asString = asString;
        }
    }

    static Stream<String> nonNegativeReps(double value) {
        String normal = decimalFormat.format(value);
        String otherZero;
        if (normal.endsWith(".0")) {
            otherZero = normal.substring(0, normal.length() - 2);
        } else {
            otherZero = normal + '0';
        }
        if (normal.charAt(0) == '0') {
            return Stream.of(normal, otherZero, normal.substring(1), otherZero.substring(1));
        } else {
            return Stream.of(normal, otherZero);
        }
    }

    static Stream<String> exponentReps(int exponent) {
        if (exponent >= 0) {
            return Stream.of(Integer.toString(exponent));
        } else {
            return Stream.of(Integer.toString(exponent), '–' + Integer.toString(-exponent));
        }
    }

    static Stream<ParseExample> negativeExactExamples(Stream<ParseExample> positiveExactExamples) {
        return positiveExactExamples.flatMap(positiveExample ->
                Arrays.stream(MINUS_CHARS).map(minus ->
                        new ParseExample(
                                RangedValue.forExact(-positiveExample.value.getLowerValue()),
                                minus + positiveExample.asString)));
    }

    // XXX This works, but is terrible. Rewrite as random generation?
    static Stream<ParseExample> parseExamples() {
        Supplier<Stream<ParseExample>> zeroSimples = () -> Arrays.stream(ZERO_REPS)
                .map(s -> new ParseExample(RangedValue.forExact(0), s));
        Supplier<Stream<ParseExample>> positiveSimples = () -> Arrays.stream(POSITIVE_NUMBERS)
                .mapToObj(value -> nonNegativeReps(value)
                        .map(s -> new ParseExample(RangedValue.forExact(value), s))
                ).flatMap(Function.identity());
        Supplier<Stream<ParseExample>> nonNegativeSimples = () -> Stream.concat(positiveSimples.get(), zeroSimples.get());
        Supplier<Stream<ParseExample>> negativeSimples = () -> negativeExactExamples(positiveSimples.get());
        Supplier<Stream<ParseExample>> simples = () -> Stream.concat(nonNegativeSimples.get(), negativeSimples.get());

        Supplier<Stream<ParseExample>> positiveExponents = () -> Arrays.stream(POSITIVE_NUMBERS)
                .mapToObj(value -> {
                    int naturalExponent = (int) Math.floor(Math.log10(value));
                    RangedValue rv = RangedValue.forExact(value);
                    return IntStream.range(naturalExponent - 1, naturalExponent + 1)
                            .mapToObj(exponent -> {
                                double significand = value / Math.pow(10, exponent);
                                return nonNegativeReps(significand).flatMap(significandRep ->
                                        exponentReps(exponent).map(exponentRep ->
                                                new ParseExample(rv, significandRep + "E" + exponentRep)));
                            }).flatMap(Function.identity());
                }).flatMap(Function.identity());
        Supplier<Stream<ParseExample>> zeroExponents = () -> Arrays.stream(ZERO_REPS).flatMap(zeroRep ->
                IntStream.range(-1, 1)
                        .mapToObj(RangedValueUnitTest::exponentReps)
                        .flatMap(reps -> reps.map(exRep ->
                                new ParseExample(RangedValue.forExact(0), zeroRep + "E" + exRep))));
        Supplier<Stream<ParseExample>> nonNegativeExponents = () -> Stream.concat(positiveExponents.get(), zeroExponents.get());
        Supplier<Stream<ParseExample>> negativeExponents = () -> negativeExactExamples(positiveExponents.get());
        Supplier<Stream<ParseExample>> exponents = () -> Stream.concat(nonNegativeExponents.get(), negativeExponents.get());

        Supplier<Stream<ParseExample>> exacts = () -> Stream.concat(simples.get(), exponents.get());
        Supplier<Stream<ParseExample>> bounds = () -> Stream.concat(nonNegativeSimples.get(), nonNegativeExponents.get());
        Supplier<Stream<ParseExample>> withBounds = () -> exacts.get().flatMap(valueExample -> bounds.get().map(radiusExample ->
                new ParseExample(
                        RangedValue.forBounds(valueExample.value.getLowerValue(), radiusExample.value.getLowerValue()),
                        valueExample.asString + '±' + radiusExample.asString)));

        return Stream.concat(exacts.get(), withBounds.get());
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

    private static void assertParses(RangedValue expected, CharSequence str) {
        RangedValue actual = RangedValue.parse(str);
        if (!expected.equals(actual)) {
            fail("Parsed «" + str + "» as " + actual + " instead of " + expected);
        }
    }

    @Test
    public void parsesCorrectInput() {
        parseExamples().forEach(example -> assertParses(example.value, example.asString));
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
