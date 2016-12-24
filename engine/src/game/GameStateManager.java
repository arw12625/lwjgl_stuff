package game;

import java.util.Deque;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 *
 * @author Andrew_2
 */
public class GameStateManager {
    private Deque<GameState> stateStack;
    
    public GameStateManager() {
        stateStack = new ConcurrentLinkedDeque<>();
    }
    
    public GameState getState() {
        return stateStack.isEmpty() ? null : stateStack.peek();
    }
    
    public void pushState(GameState inst) {
        stateStack.push(inst);
    }
    
    public void popState() {
        stateStack.pop();
    }
    
    public void setState(GameState inst) {
        popState();
        pushState(inst);
    }
    
}
