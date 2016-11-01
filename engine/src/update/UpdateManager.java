/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package update;

import game.Game;
import java.util.ArrayList;
import java.util.List;
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
public class UpdateManager implements Runnable {

    private long lastTime; //last time in milis
    private final List<Updateable> entities;
    private final long updateTime;
    private final Thread updateThread;
    public static final int defaultUpdateTime = 1000 / 60; // 60 fps
    private static UpdateManager instance;

    public static UpdateManager getInstance() {
        if (instance == null) {
            instance = new UpdateManager();
        }
        return instance;
    }

    private UpdateManager() {
        entities = new ArrayList<>();
        updateThread = new Thread(this);
        this.updateTime = defaultUpdateTime;
        lastTime = getTime();
    }

    public void destroy() {
        try {
            updateThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        updateThread.start();
    }

    @Override
    public void run() {
        Game g = Game.getInstance();
        while (g.running()) {

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
