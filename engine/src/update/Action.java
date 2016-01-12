package update;

/**
 *
 * @author Andy
 * 
 * Actions are responses to events with generic arguments
 */
public interface Action {
    public void act(Object... args);
}
