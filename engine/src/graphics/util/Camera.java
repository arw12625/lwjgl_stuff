package graphics.util;

import geometry.Transform;
import geometry.HasTransform;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 *
 * @author Andrew_2
 */
public class Camera implements HasTransform {
    //all matrices are dervied from transform
    private final Transform transform;
    private final Matrix4f viewMatrix;
    private final Matrix4f projectionMatrix;
    private final Matrix4f projectionViewMatrix;
    
    private boolean isProjectionStale;

    public Camera() {
        viewMatrix = new Matrix4f();
        projectionMatrix = new Matrix4f();
        projectionViewMatrix = new Matrix4f();
        transform = new Transform();
        transform.setAutoRefresh(false);
        staleProjection();
    }
    
    public boolean isInitialized() {
        return transform == null;
    }
    
    private void staleProjection() {
        isProjectionStale = true;
    }

    public void moveWorldCoords(Vector3f u) {
        transform.translate(u);
    }
    
    public void moveWorldCoords(float x, float y, float z) {
        transform.translate(x, y, z);
    }
    
    public void moveLocalCoords(float x, float y, float z) {
        Vector3f v = new Vector3f(x, y, z);
        transform.getOrientation(new Quaternionf()).transform(v);
        transform.translate(v);
    }

    public void rotate(Quaternionf q) {
        transform.rotate(q);
    }

    //for now we ignore the scale of the transform
    private void refresh() {
        if(transform.isStale()) {
            transform.getTransformationMatrix(viewMatrix).invert();
            transform.refreshTransform();
            staleProjection();
        }
        if(isProjectionStale) {
            projectionMatrix.mul(viewMatrix, projectionViewMatrix);
            isProjectionStale = false;
        }
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
        return transform.getScale(dest);
    }
    
    @Override
    public Matrix4f getTransformationMatrix(Matrix4f dest) {
        return transform.getTransformationMatrix(dest);
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
    
    public void setYXAngle(float x, float y) {
        transform.setOrientation(new Quaternionf()
                .rotateY(y)
                .rotateX(x));
    }
    
    public void setOrthographicProjection(float width, float height, float near, float far) {
        Matrix4f proj = new Matrix4f();
        proj.set(2 / width, 0, 0, 0,
                0, 2 / height, 0, 0,
                0, 0, -2 / (far - near), (far + near) / (far - near),
                0, 0, 0, 1);
        proj.transpose();
        setProjectionMatrix(proj);
    }

    public void setPespectiveProjection(float fovx, float aspect, float znear, float zfar) {
        Matrix4f proj = new Matrix4f();
        proj.setPerspective(fovx / aspect, aspect, znear, zfar);
        setProjectionMatrix(proj);
    }
    
    public void set2DProjection() {
        Matrix4f proj = new Matrix4f();
        proj.m22 = 0;
        setProjectionMatrix(proj);
    }

    public void setProjectionMatrix(Matrix4f projection) {
        this.projectionMatrix.set(projection);
        staleProjection();
    }
    
    public Matrix4f getViewMatrix() {
        refresh();
        return new Matrix4f(viewMatrix);
    }
    
    public Matrix4f getProjectionMatrix() {
        refresh();
        return new Matrix4f(projectionMatrix);
    }
    
    public Matrix4f getProjectionViewMatrix() {
        refresh();
        return new Matrix4f(projectionViewMatrix);
    }

}
