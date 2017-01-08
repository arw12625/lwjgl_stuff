package graphics.util;

import graphics.RenderLayer;
import graphics.ViewPort;

/**
 *
 * @author Andrew_2
 */
public class CameraViewPort extends ViewPort implements HasCamera {

    private final Camera c;
    
    public CameraViewPort(Camera c, int viewX, int viewY, int viewWidth, int viewHeight) {
        super(viewX, viewY, viewWidth, viewHeight);
        this.c = c;
    }

    @Override
    public boolean supportsLayer(RenderLayer layer) {
        return layer instanceof RenderLayer3D;
    }

    @Override
    public Camera getCamera() {
        return c;
    }
    
}
