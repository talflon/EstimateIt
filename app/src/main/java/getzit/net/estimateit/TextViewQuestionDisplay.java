package getzit.net.estimateit;

import android.widget.TextView;

public class TextViewQuestionDisplay implements QuestionDisplay {
    private final TextView textView;

    public TextViewQuestionDisplay(TextView textView) {
        this.textView = textView;
    }

    @Override
    public void displayText(String text) {
        textView.append(text);
    }

    @Override
    public void reset() {
        textView.setText("");
    }
}
