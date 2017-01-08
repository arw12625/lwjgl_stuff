package graphics.util;

import graphics.Renderable;

/**
 *
 * @author Andrew_2
 */
public abstract class RenderableAdapter implements Renderable {
    
    private boolean isInitialized, isEnabled = true, isPendingRelease, isReleased;
    
    @Override
    public void renderInit() {
        setRenderInitialized();
    }
    
    protected  void setRenderInitialized() {
        this.isInitialized = true;
    }
    
    @Override
    public void renderRelease() {
        setRenderReleased();
    }
    
    protected  void setRenderReleased() {
        this.isReleased = true;
    }
    
    @Override
    public boolean isRenderEnabled() {
        return isEnabled && isInitialized;
    }
    
    @Override
    public boolean isRenderPendingRelease() {
        return isPendingRelease;
    }
    
    @Override
    public boolean isRenderReleased() {
        return isReleased;
    }
    
    public void renderEnable(boolean enable) {
        this.isEnabled = enable;
    }
    
    public void requestRenderRelease() {
        this.isPendingRelease = true;
    }
    
}
