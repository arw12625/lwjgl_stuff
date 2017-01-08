package geometry;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 *
 * @author Andrew_2
 */
public interface HasTransform {
    public Vector3f getPosition();
    public Quaternionf getOrientation();
}
