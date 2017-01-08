package graphics.util;

import graphics.UniformBuffer;
import graphics.RenderLayer;
import graphics.Renderable;
import graphics.visual.Lighting;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import util.ZIndexSet;
import util.ZIndexSetStandard;

/**
 *
 * @author Andrew_2
 */
public class RenderLayer3D extends RenderLayer {

    public RenderLayer3D(ZIndexSet<Renderable> renderables) {
        super(renderables);
    }
    
    public static RenderLayer3D createRenderLayer3D() {
        return new RenderLayer3D(ZIndexSetStandard.<Renderable>createCopyOnWriteSet());

    }
    
}
