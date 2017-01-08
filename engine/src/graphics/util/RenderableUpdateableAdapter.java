package graphics.util;

import graphics.Renderable;
import update.Updateable;

/**
 *
 * @author Andrew_2
 */
public abstract class RenderableUpdateableAdapter implements Renderable,Updateable {
    
    private boolean isRenderInitialized, isRenderEnabled, isRenderPendingRelease, isRenderReleased;
    private boolean isUpdateInitialized, isUpdateEnabled, isUpdatePendingRelease, isUpdateReleased;
    
    
    @Override
    public void renderInit() {
        setRenderInitialized();
    }
    
    protected  void setRenderInitialized() {
        this.isRenderInitialized = true;
    }
    
    @Override
    public void renderRelease() {
        setRenderReleased();
    }
    
    protected  void setRenderReleased() {
        this.isRenderReleased = true;
    }
    
    @Override
    public boolean isRenderEnabled() {
        return isRenderEnabled && isRenderInitialized;
    }
    
    @Override
    public boolean isRenderPendingRelease() {
        return isRenderPendingRelease;
    }
    
    @Override
    public boolean isRenderReleased() {
        return isRenderReleased;
    }
    
    public void renderEnable(boolean enable) {
        this.isRenderEnabled = true;
    }
    
    public void requestRenderRelease() {
        this.isRenderPendingRelease = true;
    }
    
    @Override
    public void updateInit() {
        setUpdateInitialized();
    }
    
    protected  void setUpdateInitialized() {
        this.isUpdateInitialized = true;
    }
    
    @Override
    public void updateRelease() {
        setUpdateReleased();
    }
    
    protected  void setUpdateReleased() {
        this.isUpdateReleased = true;
    }
    
    @Override
    public boolean isUpdateEnabled() {
        return isUpdateEnabled && isUpdateInitialized;
    }
    
    @Override
    public boolean isUpdatePendingRelease() {
        return isUpdatePendingRelease;
    }
    
    @Override
    public boolean isUpdateReleased() {
        return isUpdateReleased;
    }
    
    public void updateEnable(boolean enable) {
        this.isUpdateEnabled = true;
    }
    
    public void requestUpdateRelease() {
        this.isUpdatePendingRelease = true;
    }
    
    public void setEnabled(boolean enable) {
        renderEnable(enable);
        updateEnable(enable);
    }
    
    public void requestRelease() {
        requestRenderRelease();
        requestUpdateRelease();
    }

    
    
}