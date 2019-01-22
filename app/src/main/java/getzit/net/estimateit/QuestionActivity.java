package getzit.net.estimateit;

import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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
    private AnswerKeyboardView keyboardView;

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
        answerInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateAnswerInput();
            }
        });

        resultText = findViewById(R.id.resultText);

        keyboardView = findViewById(R.id.keyboard);
        keyboardView.setKeyboard(new Keyboard(this, R.xml.answer_keyboard));
        keyboardView.setOnKeyboardActionListener(new EditTextTyper(answerInput) {
            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                switch (primaryCode) {
                    case Keyboard.KEYCODE_DONE:
                        checkAnswer();
                        break;
                    default:
                        super.onKey(primaryCode, keyCodes);
                }
            }
        });

        findViewById(R.id.buttonNext).setOnClickListener(v -> nextQuestion());

        nextQuestion();
        validateAnswerInput();
    }

    private void validateAnswerInput() {
        resultText.setText("");
        keyboardView.setActionKeyLabel(
                (parseAnswer() != null) ? getString(R.string.keylabel_check) : "");
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

    private @Nullable RangedValue parseAnswer() {
        try {
            return RangedValue.parse(answerInput.getText());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void checkAnswer() {
        RangedValue answer = parseAnswer();
        if (answer == null) {
            return;
        }
        boolean correct = answer.covers(currentQuestion.getAnswer());
        resultText.setText(correct ? R.string.answer_correct : R.string.answer_incorrect);
    }
}
