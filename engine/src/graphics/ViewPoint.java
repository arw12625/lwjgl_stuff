package graphics;

import geometry.Transform;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 *
 * @author Andy
 * 
 * ViewPoint represents a camera from which the scene can be rendered
 */
public class ViewPoint {

    Transform transform;
    Matrix4f viewMatrix;

    public ViewPoint() {

        this(new Vector3f(), new Quaternionf());

    }

    public ViewPoint(Vector3f position, Quaternionf orientation) {

        transform = new Transform(position, orientation);
        viewMatrix = new Matrix4f();
        refreshViewMatrix();

    }

    public void moveWorldCoords(Vector3f u) {

        transform.translate(u);

    }

    public void moveWorldCoords(float x, float y, float z) {

        transform.translate(x, y, z);

    }
    
    public void moveLocalCoords(float x, float y, float z) {
        Vector3f v = new Vector3f(x, y, z);
        transform.getOrientation().transform(v);
        transform.translate(v);

    }

    public void rotate(Quaternionf q) {
        transform.rotate(q);
    }

    public void refreshViewMatrix() {
        transform.getOrientation().get(viewMatrix);
        viewMatrix.setTranslation(transform.getPosition());
        viewMatrix.invert();
    }

    public void setPosition(float x, float y, float z) {

        setPosition(new Vector3f(x, y, z));

    }

    public void setPosition(Vector3f v) {
        transform.setPosition(v);
    }

    public void setOrientation(Quaternionf orientation) {
        transform.setOrientation(orientation);
    }

    public Vector3f getPosition() {
        return transform.getPosition();
    }
    
    public Matrix4f getViewMatrix() {
        refreshViewMatrix();
        return viewMatrix;
    }
    
    public void setYXAngle(float x, float y) {
        transform.getOrientation().identity().rotateY(y).rotateX(x);
    }
}