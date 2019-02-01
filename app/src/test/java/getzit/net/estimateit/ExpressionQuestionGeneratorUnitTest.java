package getzit.net.estimateit;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class ExpressionQuestionGeneratorUnitTest {
    private static String getText(Question question) {
        List<String> result = new ArrayList<>();
        question.display(new QuestionDisplay() {
            @Override
            public void displayText(String text) {
                result.add(text);
            }

            @Override
            public void reset() {
                result.clear();
            }
        });
        return String.join("", result);
    }

    @Test
    public void answerIsCorrect() {
        Random random = new Random(0x7c854e664b4c27f5L);
        RandomGenerator<Double> numberGenerator = NumberGenerators.dblFromScaleAndPrecision(RandomGenerator.exactly(-1), RandomGenerator.exactly(7));
        Context context = Context.enter();
        try {
            Scriptable scope = context.initSafeStandardObjects();
            for (int len = 2; len < 20; len++) {
                Integer length = len;
                ExpressionQuestionGenerator generator = new ExpressionQuestionGenerator(r -> length, r -> numberGenerator);
                for (int i = 0; i < 100; i++) {
                    Question question = generator.generate(random);
                    String questionText = getText(question);
                    String jsExpr = questionText
                            .replace(ExpressionQuestionGenerator.ADD, '+')
                            .replace(ExpressionQuestionGenerator.SUBTRACT, '-')
                            .replace(ExpressionQuestionGenerator.MULTIPLY, '*')
                            .replace(ExpressionQuestionGenerator.DIVIDE, '/');
                    Object result = context.evaluateString(scope, jsExpr, "<test>", 1, null);
                    double expected = Double.valueOf(result.toString());
                    if (!RangedValue.forMultiplicativeBounds(expected, 1 + 1e-11).covers(question.getAnswer())) {
                        fail("Expected answer for [" + questionText + "]: "
                                + expected + ", but got " + question.getAnswer());
                    }
                }
            }
        } finally {
            Context.exit();
        }
    }
}
