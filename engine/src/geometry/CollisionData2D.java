package geometry;

import org.joml.Vector2f;

/**
 *
 * @author Andrew_2
 */
public class CollisionData2D extends CollisionData {

    //global coordinates 
    private final Vector2f contactPosition;
    private final Vector2f contactNormal;
    
    public CollisionData2D(Collider primary, Collider secondary,
            Vector2f contactPosition, Vector2f contactNormal) {
        super(primary, secondary);
        this.contactPosition = contactPosition;
        this.contactNormal = contactNormal;
    }

    public Vector2f getContactPosition() {
        return new Vector2f(contactPosition);
    }

    public Vector2f getContactNormal() {
        return new Vector2f(contactNormal);
    }
}
