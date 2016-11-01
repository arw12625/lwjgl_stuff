package io;

import game.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.lwjgl.glfw.GLFW;

/**
 *
 * @author Andrew_2
 * 
 * An extension of keycallback to parse characters from key input
 */
public class TextInput extends KeyCallback {

    private List<TextCallback> callbacks;
    private Map<Integer, Character> keyMap;
    private Map<Integer, Character> shiftMap;

    public TextInput(Component  parent) {
        super(parent);
        callbacks = new CopyOnWriteArrayList<>();
        keyMap = defaultKeyMap;
        shiftMap = defaultShiftMap;
    }
    
    public void addTextCallback(TextCallback tc) {
        callbacks.add(tc);
    }
    
    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
        
        char c = parseChar(window, key, scancode, action, mods);
        if(c == '\u0000') {
            return;
        }
        for(TextCallback tcb : callbacks) {
            if(tcb.isDestroyed()) {
                callbacks.remove(tcb);
            } else if(tcb.isEnabled()) {
                tcb.push(c);
            }
        }
        
    }
    
    public char parseChar(long window, int key, int scancode, int action, int mods) {
        char c = '\u0000';
        if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
            Character charObj = (mods != GLFW.GLFW_MOD_SHIFT) ? keyMap.get(key) : shiftMap.get(key);

            if (charObj != null) {
                c = charObj;
            }

        }
        return c;
    }

    private static final Map<Integer, Character> defaultKeyMap;
    private static final Map<Integer, Character> defaultShiftMap;

    static {
        defaultKeyMap = new HashMap<>();
        defaultKeyMap.put(GLFW.GLFW_KEY_ENTER, '\n');
        defaultKeyMap.put(GLFW.GLFW_KEY_TAB, '\t');
        defaultKeyMap.put(GLFW.GLFW_KEY_BACKSPACE, '\b');
        defaultKeyMap.put((int) '1', '1');
        defaultKeyMap.put((int) '2', '2');
        defaultKeyMap.put((int) '3', '3');
        defaultKeyMap.put((int) '4', '4');
        defaultKeyMap.put((int) '5', '5');
        defaultKeyMap.put((int) '6', '6');
        defaultKeyMap.put((int) '7', '7');
        defaultKeyMap.put((int) '8', '8');
        defaultKeyMap.put((int) '9', '9');
        defaultKeyMap.put((int) '0', '0');
        defaultKeyMap.put((int) '-', '-');
        defaultKeyMap.put((int) '=', '=');
        defaultKeyMap.put((int) '[', '[');
        defaultKeyMap.put((int) ']', ']');
        defaultKeyMap.put((int) '\\', '\\');
        defaultKeyMap.put((int) ';', ';');
        defaultKeyMap.put((int) '\'', '\'');
        defaultKeyMap.put((int) ',', ',');
        defaultKeyMap.put((int) '.', '.');
        defaultKeyMap.put((int) '/', '/');
        defaultKeyMap.put((int) '`', '`');
        defaultKeyMap.put((int) ' ', ' ');
        for (int i = 65; i <= 90; i++) {
            defaultKeyMap.put(i, (char) (i + 32));
        }

        defaultShiftMap = new HashMap<>();
        defaultShiftMap.put(GLFW.GLFW_KEY_BACKSPACE, '\b');
        defaultShiftMap.put(GLFW.GLFW_KEY_ENTER, '\n');
        defaultShiftMap.put((int) '1', '!');
        defaultShiftMap.put((int) '2', '@');
        defaultShiftMap.put((int) '3', '#');
        defaultShiftMap.put((int) '4', '$');
        defaultShiftMap.put((int) '5', '%');
        defaultShiftMap.put((int) '6', '^');
        defaultShiftMap.put((int) '7', '&');
        defaultShiftMap.put((int) '8', '*');
        defaultShiftMap.put((int) '9', '(');
        defaultShiftMap.put((int) '0', ')');
        defaultShiftMap.put((int) '-', '_');
        defaultShiftMap.put((int) '=', '+');
        defaultShiftMap.put((int) '[', '{');
        defaultShiftMap.put((int) ']', '}');
        defaultShiftMap.put((int) '\\', '|');
        defaultShiftMap.put((int) ';', ':');
        defaultShiftMap.put((int) '\'', '\"');
        defaultShiftMap.put((int) ',', '<');
        defaultShiftMap.put((int) '.', '>');
        defaultShiftMap.put((int) '/', '?');
        defaultShiftMap.put((int) '`', '~');
        defaultShiftMap.put((int) ' ', ' ');
        for (int i = 65; i <= 90; i++) {
            defaultShiftMap.put(i, (char) i);
        }
    }

}
