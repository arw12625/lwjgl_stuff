package graphics;

import game.Component;
import game.GameObject;
import geometry.Transform;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import update.Updateable;

/**
 *
 * @author Andrew_2
 */
public class UniformTransform extends UniformStruct {

    Matrix4f initTrans;
    Transform t;
    UniformData ud;
    int pvmID, vmID, normalID;
    Matrix4f pvmMat, viewMat;
    Matrix3f normalMat;
    
    public UniformTransform(UniformData ud, Matrix4f initTrans, Transform t) {
        super(ud);
        this.t = t;
        this.ud = ud;
        this.initTrans = initTrans;
        pvmID = ud.createUniform("proj_view_model", graphics.UniformData.GL_UNIFORM_TYPE.GL_m4fv, 1);
        vmID = ud.createUniform("view_model", graphics.UniformData.GL_UNIFORM_TYPE.GL_m4fv, 1);
        normalID = ud.createUniform("normal_mat", graphics.UniformData.GL_UNIFORM_TYPE.GL_m3fv, 1);
        
        pvmMat = new Matrix4f();
        viewMat = new Matrix4f();
        normalMat = new Matrix3f();
    }
    
    @Override
    public void updateUniformStruct() {
        Matrix4f transMat = t.toMatrix();
        transMat.mul(initTrans);
        RenderManager.getInstance().getProjectionViewMatrix().mul(transMat, pvmMat);

        RenderManager.getInstance().getViewMatrix().mul(transMat, viewMat);

        normalMat.set(viewMat);
        normalMat.invert().transpose();
        
        ud.setUniform(pvmID, pvmMat);
        ud.setUniform(vmID, viewMat);
        ud.setUniform(normalID, normalMat);
    }
    
}
