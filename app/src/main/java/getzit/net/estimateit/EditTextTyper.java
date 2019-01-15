package getzit.net.estimateit;

import android.inputmethodservice.KeyboardView;
import android.view.KeyEvent;
import android.widget.EditText;

public class EditTextTyper implements KeyboardView.OnKeyboardActionListener {
    private final EditText textView;

    public EditTextTyper(EditText textView) {
        this.textView = textView;
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        switch (primaryCode) {
            case KeyEvent.KEYCODE_DEL:
                backspace();
                break;
            case KeyEvent.KEYCODE_FORWARD_DEL:
                forwardDelete();
                break;
            case KeyEvent.KEYCODE_CLEAR:
                textView.getText().clear();
                break;
        }
    }

    protected void backspace() {
        int start = textView.getSelectionStart();
        int end;
        if (start >= 0) {
            end = textView.getSelectionEnd();
            if (end == start) {
                start--;
            }
        } else {
            end = textView.getText().length();
            start = end - 1;
        }
        if (start >= 0) {
            delete(start, end);
        }
    }

    protected void forwardDelete() {
        int start = textView.getSelectionStart();
        if (start >= 0 && start < textView.getText().length()) {
            int end = textView.getSelectionEnd();
            if (end == start) {
                end++;
            }
            delete(start, end);
        }
    }

    protected void delete(int start, int end) {
        textView.getText().delete(start, end);
    }

    @Override
    public void onText(CharSequence input) {
        int start = textView.getSelectionStart();
        if (start >= 0) {
            textView.getText().replace(start, textView.getSelectionEnd(), input);
        } else {
            textView.append(input);
        }
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
}
