package io;

import game.Component;

/**
 *
 * @author Andrew_2
 */
public interface TextCallback {
    
    public abstract void pushTextChar(char keyChar);
    
    public default boolean isTextCallbackEnabled() {
        return true;
    }

    public default boolean isTextCallbackPendingRelease() {
        return false;
    }
    
}
