package geometry;

/**
 *
 * @author Andrew_2
 */
public interface CollisionFilter {
    
    public CollisionData collide(Collider primary, Collider secondary);
    
}
