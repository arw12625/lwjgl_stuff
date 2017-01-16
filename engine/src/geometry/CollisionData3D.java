package geometry;

import org.joml.Vector3f;

/**
 *
 * @author Andrew_2
 */
public class CollisionData3D extends CollisionData{

    private final Vector3f contactPos;
    private final Vector3f contactNorm;
    
    public CollisionData3D(Collider primary, Collider secondary, Vector3f contactPos, Vector3f contactNorm) {
        super(primary, secondary);
        this.contactPos = contactPos;
        this.contactNorm = contactNorm;
    }
    
    
    @Override
    public String toString() {
        return "Contact Postion: " + contactPos +
                "\nContact Normal: " + contactNorm;
    }
}
