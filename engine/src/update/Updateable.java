package update;

/**
 *
 * @author Andy
 * 
 * interface for any object that needs to be run each update cycle
 */
public interface Updateable {
    
    public void update(int delta);
    public default boolean isDestroyed() {return false;}
    public default boolean isEnabled() {return true;}
}
