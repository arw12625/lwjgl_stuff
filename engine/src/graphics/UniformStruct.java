package graphics;

/**
 *
 * @author Andrew_2
 */
public abstract class UniformStruct {
    
    private UniformData parent;
    
    public UniformStruct(UniformData parent) {
        this.parent = parent;
        parent.addStruct(this);
    }
    
    public abstract void updateUniformStruct();
}
