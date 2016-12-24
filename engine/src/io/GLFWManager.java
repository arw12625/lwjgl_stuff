package io;

import game.Game;
import game.GameStateManager;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryUtil.NULL;

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

    public GLFWManager() {

        windows = new ArrayList<>();
        windowsToInitialize = new ConcurrentLinkedQueue<>();

        numRefreshes = 0;

    }

    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public void run() {
        initializeError();
        isInitialized = true;

        while (!toRelease) {

            glfwPollEvents();

            Window w;
            while ((w = windowsToInitialize.poll()) != null) {
                w.initialize();
                windows.add(w);
            }

            int i = 0;
            while(i < windows.size()) {
                w = windows.get(i);
                if(w.toRelease()) {
                    w.destroy();
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
                Logger.getLogger(GLFWManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        releaseGLFW();
    }

    private void initializeError() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (glfwInit() != GL11.GL_TRUE) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

    }

    public void release() {
        toRelease = true;
        while(!isReleased) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(GLFWManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void releaseGLFW() {

        // Terminate GLFW and release the GLFWerrorfun
        errorCallback.release();
        glfwTerminate();

        isReleased = true;
    }

    public Window createWindow(String title, int width, int height) {
        Window window = new Window(title, width, height);
        windowsToInitialize.add(window);
        return window;
    }

    public boolean isRelased() {
        return isReleased;
    }

}
