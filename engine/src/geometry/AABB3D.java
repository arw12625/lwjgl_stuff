package geometry;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 *
 * @author Andrew_2
 */
public class AABB3D extends Collider implements HasTransform {
    private final HasTransform transform;

    public AABB3D(HasTransform t) {
        this.transform = t;
    }

    @Override
    public Vector3f getPosition(Vector3f dest) {
        return transform.getPosition(dest);
    }

    @Override
    public Quaternionf getOrientation(Quaternionf dest) {
        return transform.getOrientation(dest);
    }

    @Override
    public Vector3f getScale(Vector3f dest) {
        return getHalfDimension(dest);
    }
    
    public Vector3f getHalfDimension(Vector3f dest) {
        return transform.getScale(dest);
    }
    
    public Vector3f getDimension(Vector3f dest) {
        return getHalfDimension(dest).mul(2);
    }
    
    
}
