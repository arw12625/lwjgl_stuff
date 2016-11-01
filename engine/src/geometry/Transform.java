package geometry;

import game.Component;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 *
 * @author Andy
 * 
 * Transform represents a transformation in physical space
 * This is represented as a position and orientation and scaling
 * Transformation order is scaling then rotation then translation
 * 
 */
public class Transform extends Component {
    
    Vector3f position;
    Vector3f scale;
    Quaternionf orientation;
    private Matrix4f transform;
    
    private static final Vector3f unitScale = new Vector3f(1,1,1);
    
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
    public Transform(Component parent) {
        this(parent, new Vector3f());
    }
    public Transform(Component parent, Quaternionf orientation) {
        this(parent, new Vector3f(), orientation);
    }
    public Transform(Component parent, Vector3f position) {
        this(parent, position, new Quaternionf());
    }
    public Transform(Component parent, Transform t) {
        this(parent, t.getPosition(), t.getOrientation(), t.getScale());
    }
    public Transform(Component parent, Vector3f position, Quaternionf orientation) {
        this(parent, position, orientation, unitScale);
    }
    public Transform(Component parent, Vector3f position, Quaternionf orientation, Vector3f scale) {
        super(parent);
        this.position = position;
        this.orientation = orientation;
        this.scale = scale;
        transform = new Matrix4f();
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f v) {
        this.position = v;
    }

    public Vector3f getScale() {
        return scale;
    }
    
    public void setScale(Vector3f scale) {
        this.scale = scale;
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
        transform.scaling(scale);
        orientation.get(transform);
        transform.setTranslation(position);
        return transform;
    }
    
    public static Transform createTransform(Component parent, Matrix4f mat) {
        
        Vector3f pos3 = new Vector3f();
        mat.getTranslation(pos3);
        
        Quaternionf orientation = new Quaternionf();
        mat.get(orientation);
        
        Vector3f scale = new Vector3f();
        mat.getScaling(scale);
        
        return new Transform(parent, pos3, orientation, scale);
    }
    
}
