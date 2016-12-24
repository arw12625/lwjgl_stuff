package graphics;

import org.lwjgl.opengl.GL30;

/**
 *
 * @author Andrew_2
 */
public class VertexArrayObject {

    private RenderManager renderManager;
    private int vaoHandle;
    private boolean created;
    private boolean toRelease;

    public VertexArrayObject(RenderManager renderManager) {
        this.renderManager = renderManager;
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
    
    public RenderManager getRenderManager() {
        return renderManager;
    }
    
    public void useVAO() {
        renderManager.useVAO(this);
    }
    
    public void useAndUpdateVAO() {
        renderManager.useAndUpdateVAO(this);
    }
}
