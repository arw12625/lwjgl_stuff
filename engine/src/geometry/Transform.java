package geometry;

import game.Component;
import game.GameObject;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Quaternionf;

/**
 *
 * @author Andy
 * 
 * Transform represents a transformation in physical space
 * This is represented as a position and orientation
 * 
 * 
 */
public class Transform extends Component {
    
    Vector3f position;
    Quaternionf orientation;
    private Matrix4f transform;
    
    public Transform() {
        this(new Vector3f());
    }
    public Transform(Vector3f position) {
        this(position, new Quaternionf());
    }
    public Transform(Quaternionf orientation) {
        this(new Vector3f(), orientation);
    }
    public Transform(Vector3f position, Quaternionf orientation) {
        this(null, position, orientation);
    }
    public Transform(GameObject parent) {
        this(parent, new Vector3f());
    }
    public Transform(GameObject parent, Quaternionf orientation) {
        this(parent, new Vector3f(), orientation);
    }
    public Transform(GameObject parent, Vector3f position) {
        this(parent, position, new Quaternionf());
    }
    public Transform(GameObject parent, Transform t) {
        this(parent, t.getPosition(), t.getOrientation());
    }
    public Transform(GameObject parent, Vector3f position, Quaternionf orientation) {
        super(parent);
        this.position = position;
        this.orientation = orientation;
        transform = new Matrix4f();
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f v) {
        this.position = v;
    }

    public Quaternionf getOrientation() {
        return orientation;
    }

    public void setOrientation(Quaternionf q) {
        this.orientation = q;
    }
    
    public void translate(Vector3f v) {
        position.add(v);
    }
    
    public void translate(float x, float y, float z) {
        position.add(x, y, z);
    }
    
    public void rotate(Quaternionf q) {
        orientation.mul(q);
    }
    
    public Matrix4f toMatrix() {
        orientation.get(transform);
        transform.setTranslation(position);
        return transform;
    }
}
