package io;

import game.Component;
import game.GameObject;

/**
 *
 * @author Andrew_2
 */
public abstract class TextCallback extends Component {

    public TextCallback(GameObject parent) {
        super(parent);
    }
    
    public abstract void push(char keyChar);
    
    
}
