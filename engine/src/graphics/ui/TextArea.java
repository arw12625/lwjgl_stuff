package graphics.ui;

import game.Component;
import io.TextCallback;

/**
 *
 * @author Andrew_2
 */
public class TextArea extends TextCallback {

    private TextDisplay display;

    public TextArea(Component parent, TextDisplay display) {
        super(parent);
        this.display = display;
    }

    @Override
    public void push(char keyChar) {
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
