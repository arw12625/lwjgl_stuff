package geometry;

/**
 *
 * @author Andrew_2
 */
public abstract class CollisionSpace {
    
    public abstract void add(Collider c);
    public abstract void detectCollisions();
    public abstract CollisionData collides(Collider primary, Collider secondary);
}
