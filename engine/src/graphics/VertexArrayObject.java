package graphics;

import org.lwjgl.opengl.GL30;

/**
 *
 * @author Andrew_2
 */
public class VertexArrayObject {

    int vaoHandle;
    boolean created;
    boolean toRelease;

    public VertexArrayObject() {
        vaoHandle = -1;
    }

    public int generateVAO() {
        if(!created) {
            this.vaoHandle = GL30.glGenVertexArrays();
            created = true;
        }
        return this.vaoHandle;
    }
    public int getHandle() {
        return vaoHandle;
    }
   
    public void release() {
        toRelease = true;
    }

    protected void update() {
        if (toRelease) {
            releaseVAO();
        } else {
        }
    }

    private void releaseVAO() {
        
    }
}
