package game;

import java.util.Deque;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andrew_2
 */
public class GameStateManager {
    private Deque<GameState> stateStack;
    private static final Logger LOG = LoggerFactory.getLogger(GameStateManager.class);
    
    public GameStateManager() {
        stateStack = new ConcurrentLinkedDeque<>();
    }
    
    public GameState getState() {
        return stateStack.isEmpty() ? null : stateStack.peek();
    }
    
    public void pushState(GameState inst) {
        stateStack.push(inst);
        LOG.info("Game State pushed: {}", inst);
    }
    
    public GameState popState() {
        GameState popped = stateStack.pop();
        LOG.info("Game State popped: {}", popped);
        return popped;
    }
    
    public void setState(GameState inst) {
        popState();
        pushState(inst);
    }
    
}
