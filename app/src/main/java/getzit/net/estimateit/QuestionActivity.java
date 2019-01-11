package getzit.net.estimateit;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.widget.TextView;

import java.util.Random;

public class QuestionActivity extends AppCompatActivity {
    private TextView answerView;
    private QuestionDisplay questionDisplay;
    private Question currentQuestion;
    private RandomGenerator<Question> questionGenerator;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        random = new Random();
        questionGenerator = new ExpressionQuestionGenerator(
                NumberGenerators.integerRange(2, 6),
                RandomGenerator.exactly(NumberGenerators.dbl(0.1, 1e6, 0.1)));
        questionDisplay = new TextViewQuestionDisplay(findViewById(R.id.questionText));

        answerView = findViewById(R.id.answerText);

        final KeyboardView keyboardView = findViewById(R.id.keyboard);
        keyboardView.setKeyboard(new Keyboard(this, R.xml.answer_keyboard));
        keyboardView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {
            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                switch (primaryCode) {
                    case KeyEvent.KEYCODE_DEL:
                        CharSequence text = answerView.getText();
                        answerView.setText(text.subSequence(0, text.length() - 1));
                        break;
                    case KeyEvent.KEYCODE_CLEAR:
                        answerView.setText("");
                        break;
                    case KeyEvent.KEYCODE_ENTER:
                        checkAnswer();
                        break;
                }
            }

            @Override
            public void onText(CharSequence text) {
                answerView.append(text);
            }

            @Override
            public void swipeLeft() {
            }

            @Override
            public void swipeRight() {
            }

            @Override
            public void swipeDown() {
            }

            @Override
            public void swipeUp() {
            }

            @Override
            public void onPress(int primaryCode) {
            }

            @Override
            public void onRelease(int primaryCode) {
            }
        });

        nextQuestion();
    }

    public void nextQuestion() {
        currentQuestion = questionGenerator.generate(random);
        questionDisplay.reset();
        currentQuestion.display(questionDisplay);
        answerView.setText("");
        answerView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
    }

    public void checkAnswer() {
        RangedValue answer;
        try {
            answer = RangedValue.parse(answerView.getText());
        } catch (IllegalArgumentException e) {
            // TODO
            return;
        }
        boolean correct = answer.covers(currentQuestion.getAnswer());
        answerView.setBackgroundColor(getResources().getColor(correct ? R.color.correct : R.color.incorrect));
    }
}
