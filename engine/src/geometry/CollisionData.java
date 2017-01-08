package geometry;

/**
 *
 * @author Andrew_2
 */
public class CollisionData {

    //null CollisionData represents an abscence of collision
    private final Collider primary, secondary;
            
    public CollisionData(Collider primary, Collider secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }
    
    public Collider getPrimary() {
        return primary;
    }

    public Collider getSecondary() {
        return secondary;
    }
    
}
