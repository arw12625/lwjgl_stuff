package graphics;

import game.Component;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew_2
 */
public class RenderGroup extends Renderable {
    
    int zIndex;
    List<Renderable> renderables;

    public RenderGroup(Component parent, int zIndex) {
        super(parent);
        this.zIndex = zIndex;
        renderables = new ArrayList<>();
    }

    @Override
    public void render() {
        for(Renderable r : renderables) {
            r.render();
        }
    }
    
}
