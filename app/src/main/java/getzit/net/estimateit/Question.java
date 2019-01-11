package getzit.net.estimateit;

public interface Question {
    void display(QuestionDisplay display);
    RangedValue getAnswer();

    static Question createSimple(final String text, final RangedValue answer) {
        return new Question() {
            @Override
            public void display(QuestionDisplay display) {
                display.displayText(text);
            }

            @Override
            public RangedValue getAnswer() {
                return answer;
            }
        };
    }
}
