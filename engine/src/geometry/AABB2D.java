package geometry;

import org.joml.Vector2f;

/**
 *
 * @author Andrew_2
 */
//Axis-Aligned Bounding Box 2D
public class AABB2D extends Collider {
    
    private Vector2f center;
    private float width, height;
    
    public AABB2D(Vector2f center, float width, float height) {
        this.center = center;
        this.width = width;
        this.height = height;
    }
    

    public Vector2f getCenter() {
        return new Vector2f(center);
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
    
    
}
