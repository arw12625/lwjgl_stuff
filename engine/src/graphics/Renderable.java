package graphics;

/**
 *
 * @author Andrew_2
 * 
 * The interface for any object that is rendered
 * Allows initialization before rendering
 * The order of rendering is determined by the z-index
 * Note only the z-index upon adding affects its order
 * 
 */
public interface Renderable {
    
    //called after adding to a RenderManager
    public default void renderInit(){}
    //called every frame
    public void render(View view, RenderLayer layer);
    //called after being flagged for destruction
    public default void renderRelease(){}
    

    public default boolean isRenderEnabled(){return true;}
    public default boolean isRenderPendingRelease(){return false;}
    public default boolean isRenderReleased(){return false;}
    
}
