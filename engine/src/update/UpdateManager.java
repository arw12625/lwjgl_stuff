/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package update;

import game.Game;
import game.GameStateManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.glfw.GLFW;

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
    private final List<Updateable> entities;
    private final long updateTime;
    public static final int defaultUpdateTime = 1000 / 60; // 60 fps
    private boolean toRelease, isReleased;
    
    public UpdateManager() {
        entities = new ArrayList<>();
        this.updateTime = defaultUpdateTime;
        lastTime = getTime();
    }
    
    @Override
    public void run() {
        while (!toRelease) {

            long currentTime = getTime();
            int deltaTime = (int) (currentTime - lastTime);
            update(deltaTime);

            try {
                Thread.sleep(updateTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
            lastTime = currentTime;

        }
        isReleased = true;
    }

    private void update(int delta) {

        int i = 0;
        while(i < entities.size()) {
            //destoryed entities are reomved and not updated
            Updateable u = entities.get(i);
            if (u.isDestroyed()) {
                entities.remove(i);
            } else {
                if (u.isEnabled()) {
                    u.update(delta);
                }
                i++;
            }
        }

    }
    
    public void release() {
        toRelease = true;
        while(!isReleased) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(UpdateManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static long getTime() {
        return (long) (GLFW.glfwGetTime() * 1000);
    }

    public void add(Updateable u) {
        entities.add(u);
    }

    public void remove(Updateable u) {
        entities.remove(u);
    }
}
