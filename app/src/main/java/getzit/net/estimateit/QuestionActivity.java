package getzit.net.estimateit;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.EditText;

import java.util.Random;

public class QuestionActivity extends AppCompatActivity {
    private EditText answerInput;
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

        answerInput = findViewById(R.id.answerInput);

        final KeyboardView keyboardView = findViewById(R.id.keyboard);
        keyboardView.setKeyboard(new Keyboard(this, R.xml.answer_keyboard));
        keyboardView.setOnKeyboardActionListener(new EditTextTyper(answerInput) {
            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                switch (primaryCode) {
                    case KeyEvent.KEYCODE_ENTER:
                        checkAnswer();
                        break;
                    default:
                        super.onKey(primaryCode, keyCodes);
                }
            }
        });

        nextQuestion();
    }

    @Override
    protected void onResume() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onResume();
    }

    public void nextQuestion() {
        currentQuestion = questionGenerator.generate(random);
        questionDisplay.reset();
        currentQuestion.display(questionDisplay);
        answerInput.setText("");
        answerInput.setBackgroundColor(getResources().getColor(R.color.colorAccent));
    }

    public void checkAnswer() {
        RangedValue answer;
        try {
            answer = RangedValue.parse(answerInput.getText());
        } catch (IllegalArgumentException e) {
            // TODO
            return;
        }
        boolean correct = answer.covers(currentQuestion.getAnswer());
        answerInput.setBackgroundColor(getResources().getColor(correct ? R.color.correct : R.color.incorrect));
    }
}
