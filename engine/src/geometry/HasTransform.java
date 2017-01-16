package geometry;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 *
 * @author Andrew_2
 */
public interface HasTransform {
    public Vector3f getPosition(Vector3f dest);
    public Quaternionf getOrientation(Quaternionf dest);
    public Vector3f getScale(Vector3f dest);
    public default Matrix4f getTransformationMatrix(Matrix4f dest) {
        return util.Utilities.getTransformationMatrix(this, dest);
    }
}
