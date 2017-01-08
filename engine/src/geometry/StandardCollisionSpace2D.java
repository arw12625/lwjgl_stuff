package geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Andrew_2
 */
public class StandardCollisionSpace2D extends CollisionSpace {

    private final Queue<Collider> colliderQueue;
    private final List<Collider> colliders;
    private final StandardCollision2D standardCollision2D;
    
    public StandardCollisionSpace2D(StandardCollision2D standardCollision2D) {
        this.standardCollision2D = standardCollision2D;
        this.colliders = new ArrayList<>();
        this.colliderQueue = new ConcurrentLinkedQueue<>();
    }
    
    @Override
    public void add(Collider c) {
        colliderQueue.add(c);
    }

    @Override
    public void detectCollisions() {
        Collider c;
        while((c = colliderQueue.poll()) != null) {
            colliders.add(c);
        }
        for(int i = 0; i < colliders.size(); i++) {
            for(int j = i + 1; j < colliders.size(); j++) {
                CollisionData collisionData = collides(colliders.get(i), colliders.get(j));
                if(collisionData != null) {
                    colliders.get(i).respond(collisionData);
                    colliders.get(j).respond(collisionData);
                }
            }
        }
    }

    @Override
    public CollisionData collides(Collider primary, Collider secondary) {
        return standardCollision2D.collide(primary, secondary);
    }
    
}
