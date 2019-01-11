package getzit.net.estimateit;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;

public class ExpressionQuestionGenerator implements RandomGenerator<Question> {
    private static final char NO_OPERATION_CONTEXT = '(';
    static final char ADD = '+';
    static final char SUBTRACT = '–';
    static final char MULTIPLY = '×';
    static final char DIVIDE = '÷';

    private final NumberFormat numberFormat = new DecimalFormat("#.########");
    private final RandomGenerator<Integer> lengthGenerator;
    private final RandomGenerator<RandomGenerator<Double>> numberGeneratorGenerator;

    public ExpressionQuestionGenerator(RandomGenerator<Integer> lengthGenerator, RandomGenerator<RandomGenerator<Double>> numberGeneratorGenerator) {
        this.lengthGenerator = lengthGenerator;
        this.numberGeneratorGenerator = numberGeneratorGenerator;
    }

    @Override
    public Question generate(Random random) {
        Expr expr = generateExpr(random, lengthGenerator.generate(random), numberGeneratorGenerator.generate(random));
        return Question.createSimple(expr.asString, RangedValue.forExact(expr.value));
    }

    private static final class Expr {
        String asString;
        double value;
        char operation;

        Expr(String asString, double value, char operation) {
            this.asString = asString;
            this.value = value;
            this.operation = operation;
        }
    }

    private static char getRandomOperation(Random random) {
        switch (random.nextInt(4)) {
            case 0:
                return ADD;
            case 1:
                return SUBTRACT;
            case 2:
                return MULTIPLY;
            case 3:
                return DIVIDE;
            default:
                throw new RuntimeException();
        }
    }

    private Expr generateExpr(Random random, int length, RandomGenerator<Double> numberGenerator) {
        ArrayList<Expr> exprList = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            double number = numberGenerator.generate(random);
            exprList.add(new Expr(numberFormat.format(number), number, NO_OPERATION_CONTEXT));
        }
        while (exprList.size() > 1) {
            int index = random.nextInt(exprList.size() - 1);
            Expr left = exprList.get(index);
            Expr right = exprList.remove(index + 1);
            char operation = getRandomOperation(random);
            boolean leftParens = left.operation != NO_OPERATION_CONTEXT;
            boolean rightParens = right.operation != NO_OPERATION_CONTEXT;
            switch (operation) {
                case ADD:
                    left.value += right.value;
                    leftParens &= left.operation != ADD;
                    rightParens &= right.operation != ADD;
                    rightParens &= right.operation != SUBTRACT;
                    break;
                case SUBTRACT:
                    left.value -= right.value;
                    leftParens &= left.operation != ADD;
                    break;
                case MULTIPLY:
                    leftParens &= left.operation != MULTIPLY;
                    rightParens &= right.operation != MULTIPLY;
                    left.value *= right.value;
                    break;
                case DIVIDE:
                    left.value /= right.value;
                    break;
            }
            StringBuilder s = new StringBuilder();
            if (leftParens) {
                s.append('(').append(left.asString).append(')');
            } else {
                s.append(left.asString);
            }
            s.append(' ').append(operation).append(' ');
            if (rightParens) {
                s.append('(').append(right.asString).append(')');
            } else {
                s.append(right.asString);
            }
            left.asString = s.toString();
            left.operation = operation;
        }
        return exprList.get(0);
    }
}
