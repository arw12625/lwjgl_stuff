package graphics.util;

import graphics.UniformBuffer;
import graphics.GLBuffer;
import graphics.RenderLayer;
import graphics.RenderManager;
import graphics.View;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andrew_2
 * 
 * UniformBuffereableRenderer wraps UniformBufferable as a Renderable.
 * GLBuffers can be created for specific views and then are updated when this class is rendered with that view
 * The intended usage is to allow the creation of view-specific buffers, such as lighting
 */
public class UniformBufferableRenderer extends RenderableAdapter implements UniformBuffer {

    private final Map<View, GLBuffer> buffers;
    private final UniformBufferable uniBuf;
    
    private static final Logger LOG = LoggerFactory.getLogger(UniformBufferableRenderer.class);
    
    public UniformBufferableRenderer(UniformBufferable buf) {
        this.buffers = new ConcurrentHashMap<>();
        this.uniBuf = buf;
    }
    
    @Override
    public GLBuffer getGLBuffer(View v) {
        return buffers.get(v);
    }
    
    public GLBuffer createBuffer(String name, View v) {
        GLBuffer glBuf = uniBuf.createBuffer(name); 
        buffers.put(v, glBuf);
        return glBuf;
    }
    
    @Override
    public void render(View view, RenderLayer layer) {
        GLBuffer glBuf = buffers.get(view);
        if(glBuf == null) {
            glBuf = createBuffer(view.toString(), view);
        }
        uniBuf.writeBuffer(glBuf, view, layer);
        glBuf.updateBuffer();
    }
    
}
