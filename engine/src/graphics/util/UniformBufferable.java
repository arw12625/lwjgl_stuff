package graphics.util;

import graphics.GLBuffer;
import graphics.RenderLayer;
import graphics.RenderManager;
import graphics.View;

/**
 *
 * @author Andrew_2
 * 
 * This interface represents the ability to create and put data into a glBuffer based upon the view and layer
 */
public interface UniformBufferable {
    
    public GLBuffer createBuffer(String name);
    public void writeBuffer(GLBuffer glBuffer, View view, RenderLayer layer);
    
}
