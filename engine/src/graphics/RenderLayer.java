package graphics;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.lwjgl.opengl.GL11;
import util.ZIndexSet;
import util.ZIndexSetStandard;

/**
 *
 * @author Andrew_2
 */
public abstract class RenderLayer {

    private final ZIndexSet<Renderable> renderables;
    private final Queue<Renderable> renderablesToInit;

    public static final int POST_RENDER_INDEX = 10000;
    public static final int UI_INDEX = 5000;
    public static final int DEFAULT_INDEX = 0;
    public static final int BACKGROUND_INDEX = -5000;
    public static final int PRE_RENDER_INDEX = -10000;

    public RenderLayer(ZIndexSet<Renderable> renderables) {
        this.renderables = renderables;
        this.renderablesToInit = new ConcurrentLinkedQueue<>();
    }

    public void render(View view) {

        Renderable toInit;
        while((toInit = renderablesToInit.poll()) != null) {
            toInit.renderInit();
        }
        
        renderables.sort();
        Iterator<Renderable> renderableIterator = renderables.iterator();
        while (renderableIterator.hasNext()) {

            Renderable r = renderableIterator.next();
            //destroyed renderables are removed
            if (r.isRenderPendingRelease()) {
                removeRenderable(r);
            } else if (r.isRenderEnabled()) {
                r.render(view, this);
            }
        }
    }

    public void addRenderable(Renderable r, int zIndex) {
        if (!renderables.contains(r)) {
            renderables.add(r, zIndex);
            renderablesToInit.add(r);
        }

    }

    public void removeRenderable(Renderable r) {
        renderables.remove(r);
        renderablesToInit.remove(r);
    }
}
