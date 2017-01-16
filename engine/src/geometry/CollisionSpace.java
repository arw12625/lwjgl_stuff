package geometry;

/**
 *
 * @author Andrew_2
 */
public abstract class CollisionSpace {
    
    public abstract void add(Collider c);
    public abstract void remove(Collider c);
    public abstract void detectCollisions();
}
