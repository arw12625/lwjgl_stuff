package geometry;

import game.Component;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 *
 * @author Andy
 *
 * Transform represents a transformation in physical space This is represented
 * as a position and orientation and scaling Transformation order is scaling
 * then rotation then translation
 *
 */
public class Transform implements HasTransform {

    private final Vector3f position;
    private final Vector3f scale;
    private final Quaternionf orientation;
    private final Matrix4f matrix;

    private boolean isStale;

    private static final Vector3f unitScale = new Vector3f(1, 1, 1);

    public Transform() {
        position = new Vector3f();
        scale = new Vector3f(1, 1, 1);
        orientation = new Quaternionf();
        matrix = new Matrix4f();
        this.isStale = true;
    }

    public Transform(Vector3f position) {
        this();
        this.position.set(position);
    }

    public Transform(Vector3f position, Quaternionf orientation) {
        this(position);
        this.orientation.set(orientation);
    }

    public Transform(Vector3f position, Quaternionf orientation, Vector3f scale) {
        this(position, orientation);
        this.scale.set(scale);
    }

    public Transform(Transform t) {
        this(t.getPosition(), t.getOrientation(), t.getScale());
    }

    public void stale() {
        isStale = true;
    }

    public void set(Transform t) {
        setPosition(t.getPosition());
        setOrientation(t.getOrientation());
        setScale(t.getScale());
    }

    @Override
    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
        stale();
    }

    public Vector3f getScale() {
        return new Vector3f(scale);
    }

    public void setScale(Vector3f scale) {
        this.scale.set(scale);
        stale();
    }

    @Override
    public Quaternionf getOrientation() {
        return new Quaternionf(orientation);
    }

    public void setOrientation(Quaternionf orientation) {
        this.orientation.set(orientation);
        stale();
    }

    public void translate(Vector3f v) {
        position.add(v);
        stale();
    }

    public void translate(float x, float y, float z) {
        position.add(x, y, z);
        stale();
    }

    public void rotate(Quaternionf q) {
        orientation.mul(q);
        stale();
    }

    private void refresh() {
        if (isStale) {
            orientation.get(matrix);
            matrix.scale(scale);
            matrix.setTranslation(position);
            isStale = false;
        }
    }

    public Matrix4f getMatrix4f() {
        refresh();

        return new Matrix4f(matrix);
    }

    public static Transform createTransform(Matrix4f mat) {

        Vector3f pos3 = new Vector3f();
        mat.getTranslation(pos3);

        Quaternionf orientation = new Quaternionf();
        mat.get(orientation);

        Vector3f scale = new Vector3f();
        mat.getScaling(scale);

        return new Transform(pos3, orientation, scale);
    }

}
