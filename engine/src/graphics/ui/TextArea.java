package graphics.ui;

import io.TextCallback;

/**
 *
 * @author Andrew_2
 */
public class TextArea implements TextCallback {

    private final TextDisplay display;

    public TextArea(TextDisplay display) {
        this.display = display;
    }

    @Override
    public void pushTextChar(char keyChar) {
        if(keyChar == '\b') {
            display.popChar();
        } else {
            if(keyChar != '\u0000') {
                display.pushChar(keyChar);
            }
        }
    }
    
    @Override
    public String toString() {
        return getText();
    }

    public String getText() {
        return display.getText();
    }
}
