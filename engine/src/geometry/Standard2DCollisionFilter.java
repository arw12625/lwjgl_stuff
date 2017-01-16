package geometry;

import org.joml.Vector2f;

/**
 *
 * @author Andrew_2
 */
public class Standard2DCollisionFilter implements CollisionFilter {
    
    @Override
    public CollisionData2D collide(Collider primary, Collider secondary) {
        if(primary instanceof AABB2D && secondary instanceof AABB2D) {
            return collideAABB2D_AABB2D((AABB2D)primary, (AABB2D)secondary);
        }
        //StandardCollision2D cannot handle these colliders
        return null;
    }
    
    
    public CollisionData2D collideAABB2D_AABB2D(AABB2D primary, AABB2D secondary) {
        
        Vector2f priCenter = primary.getCenter();
        Vector2f secCenter = secondary.getCenter();
        float priWidth = primary.getWidth();
        float priHeight = primary.getHeight();
        float secWidth = secondary.getWidth();
        float secHeight = secondary.getHeight();
        
        //distance measured primary to secondary
        //distance from top of primary to bottom of secondary
        float topDistance = (secCenter.y - secHeight / 2) - (priCenter.y + priHeight / 2);
        //distance from bottom of primary to top of secondary
        float bottomDistance = (secCenter.y + secHeight / 2) - (priCenter.y - priHeight / 2);
        if(topDistance >= 0 || bottomDistance <= 0) {
            return null;
        }
        
        //distance from right of primary to left of secondary
        float rightDistance = (secCenter.x - secWidth / 2) - (priCenter.x + priWidth / 2);
        //distance from left of primary to right of secondary
        float leftDistance = (secCenter.x + secWidth / 2) - (priCenter.x - priWidth / 2);
        if(rightDistance >= 0 || leftDistance <= 0) {
            return null;
        }
        
        Vector2f pos = new Vector2f();
        Vector2f norm = new Vector2f();
        if(rightDistance < -leftDistance) {
            pos.x = ((priCenter.x - priWidth / 2) + (secCenter.x + secWidth / 2)) / 2;
            norm.x = -rightDistance;
        } else {
            pos.x =  ((priCenter.x + priWidth / 2) + (secCenter.x - secWidth / 2)) / 2;
            norm.x = -leftDistance;
        }
        if(topDistance < -bottomDistance) {
            pos.y =  ((priCenter.y - priHeight / 2) + (secCenter.y + secHeight / 2)) / 2;
            norm.y = -topDistance;
        } else {
            pos.y =  ((priCenter.y + priHeight / 2) + (secCenter.y - secHeight / 2)) / 2;
            norm.y = -bottomDistance;
        }
        if(Math.abs(norm.y) > Math.abs(norm.x)) {
            norm.x = 0;
        } else {
            norm.y = 0;
        }
        
        return new CollisionData2D(primary, secondary, pos, norm);
    }
}
