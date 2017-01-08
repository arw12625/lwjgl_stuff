package update;

/**
 *
 * @author Andy
 * 
 * interface for any object that needs to be run each update cycle
 */
public interface Updateable {
    
    public default void updateInit(){}
    public void update(int delta, UpdateLayer layer);
    public default void updateRelease(){}
    
    public default boolean isUpdatePendingRelease() {return false;}
    public default boolean isUpdateReleased() {return false;}
    public default boolean isUpdateEnabled() {return true;}
}
