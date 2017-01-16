package graphics.util;

import geometry.HasTransform;
import graphics.util.Camera;
import geometry.Transform;
import graphics.UniformData;
import graphics.UniformStruct;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 *
 * @author Andrew_2
 */
public class UniformTransform implements UniformStruct {

    private Camera cam;
    private Matrix4f initTrans;
    private HasTransform t;
    private Matrix4f pMat, pvmMat, vmMat;
    private Matrix3f normalMat;

    private final boolean initTransEnabled, pMatEnabled, pvmMatEnabled, vmMatEnabled, normalEnabled;

    private static final String pName = "proj",
            pvmName = "proj_view_model", vmName = "view_model",
            normalName = "normal_mat";

    public UniformTransform(HasTransform t, Matrix4f initTrans,
            boolean initTransEnabled, boolean pMatEnabled,
            boolean pvmMatEnabled, boolean vmMatEnabled, 
            boolean normalEnabled) {

        this.t = t;
        this.initTrans = initTrans;

        this.initTransEnabled = initTransEnabled;
        this.pMatEnabled = pMatEnabled;
        this.pvmMatEnabled = pvmMatEnabled;
        this.vmMatEnabled = vmMatEnabled;
        this.normalEnabled = normalEnabled;

        if (pMatEnabled) {
            pMat = new Matrix4f();
        }
        if (pvmMatEnabled) {
            pvmMat = new Matrix4f();
        }
        if (vmMatEnabled || normalEnabled) {
            vmMat = new Matrix4f();
        }
        if (normalEnabled) {
            normalMat = new Matrix3f();
        }
    }

    public UniformTransform(Transform t, boolean pvmMatEnabled,
            boolean vmMatEnabled, boolean normalEnabled) {
        this(t, null, false, false, pvmMatEnabled, vmMatEnabled, normalEnabled);

    }

    public UniformTransform(Transform t) {
        this(t, true, true, true);
    }

    public UniformTransform(Transform t, Matrix4f initTrans) {
        this(t, initTrans, true, false, true, true, true);
    }

    @Override
    public void createUniformStruct(UniformData parent) {
        if (pMatEnabled) {
            parent.createUniform(pName, graphics.GLType.GL_m4fv, 1);
        }
        if (pvmMatEnabled) {
            parent.createUniform(pvmName, graphics.GLType.GL_m4fv, 1);
        }
        if (vmMatEnabled) {
            parent.createUniform(vmName, graphics.GLType.GL_m4fv, 1);
        }
        if (normalEnabled) {
            parent.createUniform(normalName, graphics.GLType.GL_m3fv, 1);
        }
    }

    @Override
    public void updateUniformStruct(UniformData parent) {
        Matrix4f transMat = t.getTransformationMatrix(new Matrix4f());
        if (initTransEnabled) {
            transMat.mul(initTrans);
        }

        if (pMatEnabled) {
            pMat.set(cam.getProjectionMatrix());
            int pID = parent.getUniform(pName);
            parent.setUniform(pID, pMat);
        }

        if (pvmMatEnabled) {
            cam.getProjectionViewMatrix().mul(transMat, pvmMat);
            int pvmID = parent.getUniform(pvmName);
            parent.setUniform(pvmID, pvmMat);
        }

        if (vmMatEnabled || normalEnabled) {
            cam.getViewMatrix().mul(transMat, vmMat);
        }
        
        if(vmMatEnabled) {
            int vmID = parent.getUniform(vmName);
            parent.setUniform(vmID, vmMat);
        }

        if (normalEnabled) {
            normalMat.set(vmMat);
            normalMat.invert().transpose();
            int normalID = parent.getUniform(normalName);
            parent.setUniform(normalID, normalMat);
        }

    }
    
    public void setCamera(Camera c) {
        this.cam = c;
    }

}
