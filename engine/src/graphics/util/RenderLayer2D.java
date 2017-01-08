package graphics.util;

import graphics.RenderLayer;
import graphics.Renderable;
import graphics.View;
import org.lwjgl.opengl.GL11;
import util.ZIndexSet;
import util.ZIndexSetStandard;

/**
 *
 * @author Andrew_2
 */
public class RenderLayer2D extends RenderLayer {

    public RenderLayer2D(ZIndexSet<Renderable> renderables) {
        super(renderables);
    }
    
    @Override
    public void render(View view) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        super.render(view);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
    
    
    public static RenderLayer2D createRenderLayer2D() {
        return new RenderLayer2D(ZIndexSetStandard.<Renderable>createCopyOnWriteSet());

    }
    
    
}
