package io;

import game.Game;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL11;
import util.LoggingOutputStream;

/**
 *
 * @author Andrew_2
 *
 * The interface with GLFW that handles windowing, opengl, and input
 */
public class GLFWManager implements Runnable {

    private List<Window> windows;
    private Queue<Window> windowsToInitialize;

    private GLFWErrorCallback errorCallback;

    private long numRefreshes;
    private boolean toRelease;
    private boolean isReleased;
    private boolean isInitialized;
    
    private static final Logger LOG = LoggerFactory.getLogger(GLFWManager.class);

    public GLFWManager() {

        LOG.info("GLFWManager constructor exited");
        windows = new ArrayList<>();
        windowsToInitialize = new ConcurrentLinkedQueue<>();

        numRefreshes = 0;
        LOG.info("GLFWManager constructor exited");

    }

    public boolean isInitialized() {
        return isInitialized;
    }

    private void initialize() {
        LOG.info("GLFWManager init entered");
        initializeError();
        isInitialized = true;
        LOG.info("GLFWManager init exited");
    }
    
    @Override
    public void run() {
        LOG.info(Game.threadMarker, "GLFW");
        LOG.info("GLFWManager run");
        initialize();

        while (!toRelease) {

            glfwPollEvents();

            Window w;
            while ((w = windowsToInitialize.poll()) != null) {
                w.glfwInitialize();
                windows.add(w);
            }

            int i = 0;
            while(i < windows.size()) {
                w = windows.get(i);
                if(w.toRelease()) {
                    w.glfwRelease();
                    windows.remove(i);
                } else {
                    w.refresh();
                    i++;
                }
            }

            numRefreshes++;
            
            try {
                Thread.sleep(1000/60);
            } catch (InterruptedException ex) {
                LOG.error("{}", ex);
            }
        }

        releaseGLFW();
    }

    private void initializeError() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(
                new PrintStream(new LoggingOutputStream(LOG), true)));

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (glfwInit() != GL11.GL_TRUE) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

    }

    public void release() {
        
        LOG.info("GLFWManager release entered");
        toRelease = true;
        while(!isReleased) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                LOG.error("{}", ex);
            }
        }
        LOG.info("GLFWManager release exited");
    }

    private void releaseGLFW() {

        // Terminate GLFW and release the GLFWerrorfun
        errorCallback.release();
        glfwTerminate();

        isReleased = true;
    }

    protected void addWindow(Window window) {
        windowsToInitialize.add(window);
        LOG.info("Window added to GLFWManager: {}", window.getTitle());
    }

    public boolean isRelased() {
        return isReleased;
    }

}
