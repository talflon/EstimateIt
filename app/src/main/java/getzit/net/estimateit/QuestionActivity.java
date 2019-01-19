package getzit.net.estimateit;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;

public class QuestionActivity extends AppCompatActivity {
    private EditText answerInput;
    private TextView resultText;
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
        resultText = findViewById(R.id.resultText);

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
                        resultText.setText("");
                        super.onKey(primaryCode, keyCodes);
                }
            }
        });

        findViewById(R.id.buttonNext).setOnClickListener(v -> nextQuestion());

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
        resultText.setText("");
    }

    public void checkAnswer() {
        RangedValue answer;
        try {
            answer = RangedValue.parse(answerInput.getText());
        } catch (IllegalArgumentException e) {
            resultText.setText(R.string.answer_invalid);
            return;
        }
        boolean correct = answer.covers(currentQuestion.getAnswer());
        resultText.setText(correct ? R.string.answer_correct : R.string.answer_incorrect);
    }
}
