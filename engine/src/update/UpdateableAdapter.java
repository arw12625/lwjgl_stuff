package update;

/**
 *
 * @author Andrew_2
 */
public abstract class UpdateableAdapter implements Updateable {
 
    private boolean isInitialized, isEnabled, isPendingRelease, isReleased;
    
    @Override
    public void updateInit() {
        setUpdateInitialized();
    }
    
    protected  void setUpdateInitialized() {
        this.isInitialized = true;
    }
    
    @Override
    public void updateRelease() {
        setUpdateReleased();
    }
    
    protected  void setUpdateReleased() {
        this.isReleased = true;
    }
    
    @Override
    public boolean isUpdateEnabled() {
        return isEnabled && isInitialized;
    }
    
    @Override
    public boolean isUpdatePendingRelease() {
        return isPendingRelease;
    }
    
    @Override
    public boolean isUpdateReleased() {
        return isReleased;
    }
    
    public void updateEnable(boolean enable) {
        this.isEnabled = true;
    }
    
    public void requestUpdateRelease() {
        this.isPendingRelease = true;
    }
    
}
