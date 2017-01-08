package graphics.util;

import graphics.RenderLayer;
import graphics.ViewPort;

/**
 *
 * @author Andrew_2
 */
public class UIViewPort extends ViewPort {

    public UIViewPort(int viewX, int viewY, int viewWidth, int viewHeight) {
        super(viewX, viewY, viewWidth, viewHeight);
    }

    @Override
    public boolean supportsLayer(RenderLayer layer) {
        return layer instanceof RenderLayer2D;
    }
    
}
