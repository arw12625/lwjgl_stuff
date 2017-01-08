package geometry;

/**
 *
 * @author Andrew_2
 */
public class Collider {
    
    private CollisionResponse response;
    
    public void setCollisionResponse(CollisionResponse response) {
        this.response = response;
    }
    
    public void respond(CollisionData data) {
        response.respond(data);
    }
}
