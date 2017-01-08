/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package update;

import game.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lwjgl.glfw.GLFW;
import util.ZIndexSet;
import util.ZIndexSetStandard;

/**
 *
 * @author Andy
 * 
 * UpdateManager handles game logic
 * Logic is run in a separate thread from rendering and resource management
 * A list of Updateables is updated each frame with the time since last update
 * 
 */
public class UpdateManager implements Runnable{

    private long lastTime; //last time in milis
    private final ZIndexSet<UpdateLayer> layers;
    private final long updateTime;
    public static final int defaultUpdateTime = 1000 / 60; // 60 fps
    private boolean toRelease, isReleased;
    
    private static final Logger LOG = LoggerFactory.getLogger(UpdateManager.class);
    
    public UpdateManager() {
        LOG.info("UpdateManager constructor entered");
        
        layers = ZIndexSetStandard.<UpdateLayer>createCopyOnWriteSet();
        
        this.updateTime = defaultUpdateTime;
        lastTime = getTime();
        LOG.info("UpdateManager constructor exited");
    }
    
    @Override
    public void run() {
        LOG.info(Game.threadMarker, "Update");
        LOG.info("UpdateManager run");
        while (!toRelease) {

            long currentTime = getTime();
            int deltaTime = (int) (currentTime - lastTime);
            for(UpdateLayer layer : layers) {
                layer.update(deltaTime);
            }
            
            try {
                Thread.sleep(updateTime);
            } catch (Exception e) {
                LOG.error("{}", e);
            }
            lastTime = currentTime;

        }
        isReleased = true;
    }
    
    public void release() {
        LOG.info("UpdateManager release entered");
        toRelease = true;
        while(!isReleased) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                LOG.error("{}", ex);
            }
        }
        LOG.info("UpdateManager release exited");
    }

    public static long getTime() {
        return (long) (GLFW.glfwGetTime() * 1000);
    }

    public void addUpdateLayer(UpdateLayer layer, int zIndex) {
        layers.add(layer, zIndex);
    }

    public void remove(UpdateLayer layer) {
        layers.remove(layer);
    }
}
