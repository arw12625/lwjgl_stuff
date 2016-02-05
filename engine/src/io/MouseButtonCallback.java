package io;

import game.Component;

/**
 *
 * @author Andrew_2
 * 
 * Interface for mouse event handling
 */
public abstract class MouseButtonCallback extends Component {
    public MouseButtonCallback(Component parent) {super(parent);}
    public abstract void invoke(long window, int button, int action, int mods);
}
