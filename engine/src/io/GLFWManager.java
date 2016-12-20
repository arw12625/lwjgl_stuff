package io;

import game.Game;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
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
public class GLFWManager {

    private List<Window> windows;
    private Queue<Window> windowsToInitialize;
    
    private GLFWErrorCallback errorCallback;
    
    private long numRefreshes;
    private boolean toDestroy;
    private boolean destroyed;

    private static GLFWManager instance;

    public static GLFWManager getInstance() {
        if (instance == null) {
            instance = new GLFWManager();
        }
        return instance;
    }
    
    private GLFWManager() {
        
        windows = new ArrayList<>();
        windowsToInitialize = new ConcurrentLinkedQueue<>();
        
        numRefreshes = 0;
        
        initializeError();
        
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

    //update glfw, called each frame by the main thread
    public void refresh() {

        if(toDestroy) {
            destroyGLFW();
            return;
        }
        
        
        glfwPollEvents();
        
        Window w;
        while ((w = windowsToInitialize.poll()) != null) {
            w.initialize();
            windows.add(w);
        }
        
        for(Window window : windows) {
            window.refresh();
        }
        
        numRefreshes++;
    }

    public void destroy() {
        toDestroy = true;
    }

    private void destroyGLFW() {
        
        for(Window w : windows) {
            w.destroy();
        }

        // Terminate GLFW and release the GLFWerrorfun
        errorCallback.release();
        glfwTerminate();
        
        destroyed = true;
    }
    
    public Window createWindow(String title, int width, int height) {
        Window window = new Window(title, width, height);
        windowsToInitialize.add(window);
        return window;
    }
    
    public boolean getIsDestroyed() {
        return destroyed;
    }
    

}
