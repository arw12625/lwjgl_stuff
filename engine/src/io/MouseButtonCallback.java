package io;

import game.Component;

/**
 *
 * @author Andrew_2
 * 
 * Interface for mouse event handling
 */
public interface MouseButtonCallback {
    public void invokeMouseButton(long window, int button, int action, int mods);
    
    public default boolean isMouseButtonCallbackEnabled() {
        return true;
    }

    public default boolean isMouseButtonCallbackPendingRelease() {
        return false;
    }
}
