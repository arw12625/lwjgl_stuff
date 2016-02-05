package graphics;

import game.Component;

/**
 *
 * @author Andrew_2
 * 
 * The parent class for any object that is rendered
 * Allows initialization before rendering
 * The order of rendering is determined by the z-index
 * Note only the z-index upon adding affects its order
 * 
 */
public abstract class Renderable extends Component {

    boolean initialized;
    
    public Renderable(Component parent) {
        super(parent);
        initialized = false;
    }
    
    //only to be called by RenderManager, not to override
    void internalInit() {
        initRender();
        initialized = true;
    }
    
    public boolean initialized() {
        return initialized;
    }
    
    //to be overridden
    public void initRender(){};
    
    //called every frame
    public abstract void render();
    
    public int getZIndex() {return RenderManager.DEFAULT_Z_INDEX;}
    
}
