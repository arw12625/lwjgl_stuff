package graphics;


/**
 *
 * @author Andrew_2
 */
public abstract class View {
     
    public abstract void refresh(RenderLayer layer);
    
    public abstract boolean supportsLayer(RenderLayer layer);
    
}
