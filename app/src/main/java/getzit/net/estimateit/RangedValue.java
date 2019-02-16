package getzit.net.estimateit;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RangedValue {
    private final double lowerValue, upperValue;

    public RangedValue(double value1, double value2) {
        if (value1 <= value2) {
            this.lowerValue = value1;
            this.upperValue = value2;
        } else {
            this.lowerValue = value2;
            this.upperValue = value1;
        }
    }

    public RangedValue(double... values) {
        Arrays.sort(values);
        this.lowerValue = values[0];
        this.upperValue = values[values.length - 1];
    }

    public static RangedValue forBounds(double expected, double radius) {
        return new RangedValue(expected - radius, expected + radius);
    }

    public static RangedValue forMultiplicativeBounds(double expected, double times) {
        return new RangedValue(expected / times, expected * times);
    }

    public static RangedValue forExact(double value) {
        return new RangedValue(value, value);
    }

    public double getLowerValue() {
        return lowerValue;
    }

    public double getUpperValue() {
        return upperValue;
    }

    public boolean covers(RangedValue other) {
        return lowerValue <= other.lowerValue && other.upperValue <= upperValue;
    }

    public RangedValue add(RangedValue other) {
        return new RangedValue(lowerValue + other.lowerValue, upperValue + other.upperValue);
    }

    public RangedValue subtract(RangedValue other) {
        return new RangedValue(lowerValue - other.upperValue, upperValue - other.lowerValue);
    }

    public RangedValue multiply(RangedValue other) {
        return new RangedValue(
                lowerValue * other.lowerValue,
                lowerValue * other.upperValue,
                upperValue * other.lowerValue,
                upperValue * other.upperValue);
    }

    static final Pattern p = Pattern.compile(
            "([–-]? (?:\\d*\\.)?\\d+ (?: E [–-]? \\d+)?) # group 1, the central value\n"
            + "(?:± ((?:\\d*\\.)?\\d+ (?: E [–-]? \\d+)? ) )? # optional group 2, the radius\n",
            Pattern.COMMENTS);

    public static RangedValue parse(CharSequence s) {
        Matcher m = p.matcher(s);
        if (!m.matches()) {
            throw new IllegalArgumentException("Could not parse \"" + s + '"');
        }
        double value = Double.parseDouble(m.group(1).replace('–', '-'));
        double radius = (m.group(2) == null) ? 0.0 : Double.parseDouble(m.group(2).replace('–', '-'));
        return RangedValue.forBounds(value, radius);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RangedValue that = (RangedValue) o;
        return Double.compare(that.lowerValue, lowerValue) == 0 &&
                Double.compare(that.upperValue, upperValue) == 0;
    }

    @Override
    public int hashCode() {
        long lowerBits = Double.doubleToLongBits(lowerValue);
        long upperBits = Double.doubleToLongBits(upperValue);
        final int prime = 173;
        int result = 1;
        result = result * prime + (int) lowerBits;
        result = result * prime + (int) (lowerBits >>> 32);
        result = result * prime + (int) upperBits;
        result = result * prime + (int) (upperBits >>> 32);
        return result;
    }

    @Override
    public String toString() {
        return "RangedValue{" +
                "lowerValue=" + lowerValue +
                ", upperValue=" + upperValue +
                '}';
    }
}
