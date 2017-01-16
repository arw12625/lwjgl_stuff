package geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 *
 * @author Andrew_2
 */
public class StandardCollisionSpace extends CollisionSpace {

    private final Set<Collider> colliders;
    private final CollisionFilter filter;
    
    public StandardCollisionSpace(CollisionFilter filter) {
        this.filter = filter;
        this.colliders = new CopyOnWriteArraySet<>();
    }
    
    @Override
    public void add(Collider c) {
        colliders.add(c);
    }

    @Override
    public void remove(Collider c) {
        colliders.remove(c);
    }

    @Override
    public void detectCollisions() {
        
        List<Collider> copy = new ArrayList<>(colliders);
        
        for(int i = 0; i < copy.size(); i++) {
            for(int j = i + 1; j < copy.size(); j++) {
                CollisionData collisionData = filter.collide(copy.get(i), copy.get(j));
                if(collisionData != null) {
                    copy.get(i).respond(collisionData);
                    copy.get(j).respond(collisionData);
                }
            }
        }
    }
}
