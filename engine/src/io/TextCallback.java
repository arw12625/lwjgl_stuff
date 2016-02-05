package io;

import game.Component;

/**
 *
 * @author Andrew_2
 */
public abstract class TextCallback extends Component {

    public TextCallback(Component parent) {
        super(parent);
    }
    
    public abstract void push(char keyChar);
    
    
}
