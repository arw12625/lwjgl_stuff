package graphics;

import game.Component;
import geometry.Transform;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import update.Updateable;

/**
 *
 * @author Andrew_2
 */
public class UniformTransform implements UniformStruct {

    private Matrix4f initTrans;
    private Transform t;
    private Matrix4f pvmMat, viewMat;
    private Matrix3f normalMat;
    
    private static final String pvmName = "proj_view_model",
            vmName = "view_model", normalName = "normal_mat";
    
    public UniformTransform(Matrix4f initTrans, Transform t) {
        this.t = t;
        this.initTrans = initTrans;
        
        pvmMat = new Matrix4f();
        viewMat = new Matrix4f();
        normalMat = new Matrix3f();
    }
    
    @Override
    public void createUniformStruct(UniformData parent) {
        parent.createUniform(pvmName, graphics.GLType.GL_m4fv, 1);
        parent.createUniform(vmName, graphics.GLType.GL_m4fv, 1);
        parent.createUniform(normalName, graphics.GLType.GL_m3fv, 1);
    }
    
    @Override
    public void updateUniformStruct(UniformData parent) {
        Matrix4f transMat = t.toMatrix();
        transMat.mul(initTrans);
        RenderManager.getInstance().getProjectionViewMatrix().mul(transMat, pvmMat);

        RenderManager.getInstance().getViewMatrix().mul(transMat, viewMat);

        normalMat.set(viewMat);
        normalMat.invert().transpose();
        
        int pvmID = parent.getUniform(pvmName);
        int vmID = parent.getUniform(vmName);
        int normalID = parent.getUniform(normalName);
        
        parent.setUniform(pvmID, pvmMat);
        parent.setUniform(vmID, viewMat);
        parent.setUniform(normalID, normalMat);
    }
    
}
