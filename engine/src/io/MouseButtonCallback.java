package io;

import game.Component;
import game.GameObject;

/**
 *
 * @author Andrew_2
 * 
 * Interface for mouse event handling
 */
public abstract class MouseButtonCallback extends Component {
    public MouseButtonCallback(GameObject parent) {super(parent);}
    public abstract void invoke(long window, int button, int action, int mods);
}
