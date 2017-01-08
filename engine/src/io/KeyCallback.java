package io;

import game.Component;

/**
 *
 * @author Andrew_2
 *
 * interface for key event handling
 */
public interface KeyCallback {

    public void invokeKey(long window, int key, int scancode, int action, int mods);

    public default boolean isKeyCallbackEnabled() {
        return true;
    }

    public default boolean isKeyCallbackPendingRelease() {
        return false;
    }

}
