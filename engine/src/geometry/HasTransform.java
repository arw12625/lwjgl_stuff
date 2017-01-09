package geometry;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 *
 * @author Andrew_2
 */
public interface HasTransform {
    public Vector3f getPosition();
    public Quaternionf getOrientation();
    public Vector3f getScale();
    public Matrix4f getTransformationMatrix();
}
