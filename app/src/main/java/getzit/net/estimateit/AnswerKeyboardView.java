package getzit.net.estimateit;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

public class AnswerKeyboardView extends KeyboardView {
    private Keyboard.Key actionKey;
    private int actionKeyIndex;

    public AnswerKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnswerKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setKeyboard(Keyboard keyboard) {
        super.setKeyboard(keyboard);
        int index = 0;
        for (Keyboard.Key key : keyboard.getKeys()) {
            if (key.codes[0] == Keyboard.KEYCODE_DONE) {
                actionKey = key;
                actionKeyIndex = index;
            }
            index++;
        }
    }

    public void setActionKeyLabel(CharSequence label) {
        if (actionKey != null && !actionKey.label.equals(label)) {
            actionKey.label = label;
            invalidateKey(actionKeyIndex);
        }
    }
}
