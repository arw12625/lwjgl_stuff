package io;

import game.Component;

/**
 *
 * @author Andrew_2
 * 
 * interface for key event handling
 */
public abstract class KeyCallback extends Component {
    public KeyCallback(Component parent) {
        super(parent);
    }
    public abstract void invoke(long window, int key, int scancode, int action, int mods);
}
