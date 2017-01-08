package graphics;

import game.Component;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Andrew_2
 */
public abstract class ViewPort extends View {

    private int viewX, viewY;
    private int viewWidth, viewHeight;
    
    public ViewPort(int viewX, int viewY, int viewWidth, int viewHeight) {
        this.viewX = viewX;
        this.viewY = viewY;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    public int getViewX() {
        return viewX;
    }

    public int getViewY() {
        return viewY;
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public int getViewHeight() {
        return viewHeight;
    }
    
    @Override
    public void refresh(RenderLayer layer) {
        GL11.glViewport(viewX, viewY, viewWidth, viewHeight);
    }

    
    
}
