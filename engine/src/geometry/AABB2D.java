package geometry;

import org.joml.Vector2f;

/**
 *
 * @author Andrew_2
 */
//Axis-Aligned Bounding Box 2D
public class AABB2D extends Collider {
    
    private final Vector2f center;
    private float width, height;
    
    public AABB2D() {
        center = new Vector2f();
    }
    
    public void setPosition(float x, float y) {
        center.x = x;
        center.y = y;
    }
    
    public void setWidth(float width) {
        this.width = width;
    }
    
    public void setHeight(float height) {
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
