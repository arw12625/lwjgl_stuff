package io;

import game.Game;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import org.lwjgl.BufferUtils;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 *
 * @author Andrew_2
 * 
 * The interface with GLFW
 * GLFW interfaces with opengl and IO, handles opengl errors
 * GLFWManager manages the display, key, and mouse functions
 * Events handled with callbacks
 * MouseData also available with getMouseX() and getMouseY()
 * 
 */
public class GLFWManager {

    private GLFWErrorCallback errorCallback;
    private GLFWWindowCloseCallback exitCallback;
    private GLFWKeyCallback keyCallback;
    private GLFWMouseButtonCallback mouseButtonCallback;

    
    private ArrayList<KeyCallback> keyCallbacks;
    private ArrayList<MouseButtonCallback> mouseButtonCallbacks;

    private DoubleBuffer mouseXBuffer, mouseYBuffer;
    private float mouseX, mouseY;

    // The window handle
    private long window;

    String title = "test";
    int width = 640, height = 480;

    private static GLFWManager instance;

    public static GLFWManager getInstance() {
        if (instance == null) {
            instance = new GLFWManager();
        }
        return instance;
    }

    private GLFWManager() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (glfwInit() != GL11.GL_TRUE) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
                window,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the ContextCapabilities instance and makes the OpenGL
        // bindings available for use.
        //GLContext.createFromCurrent();
        GL.createCapabilities();
        
        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        exitCallback = new GLFWWindowCloseCallback() {

            @Override
            public void invoke(long windowE) {
                if (windowE == window) {
                    Game.getInstance().requestQuit();
                }
            }
        };
        glfwSetWindowCloseCallback(window, exitCallback);

        mouseButtonCallbacks = new ArrayList<>();
        mouseButtonCallback = new GLFWMouseButtonCallback() {

            @Override
            public void invoke(long window, int button, int action, int mods) {
                for (int i = 0; i < mouseButtonCallbacks.size(); i++) {
                        if (mouseButtonCallbacks.get(i).isDestroyed()) {
                            mouseButtonCallbacks.remove(i);
                            i--;
                        } else if (mouseButtonCallbacks.get(i).isEnabled()) {
                            mouseButtonCallbacks.get(i).invoke(window, button, action, mods);
                        }
                    }
            }
        };
        
        
        mouseXBuffer = BufferUtils.createDoubleBuffer(1);
        mouseYBuffer = BufferUtils.createDoubleBuffer(1);
        
        keyCallbacks = new ArrayList<>();
        
        //pressing escape exits the game by default
        keyCallback = new GLFWKeyCallback() {

            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                    Game.getInstance().requestQuit();
                } else {
                    for (int i = 0; i < keyCallbacks.size(); i++) {
                        if (keyCallbacks.get(i).isDestroyed()) {
                            keyCallbacks.remove(i);
                            i--;
                        } else if (keyCallbacks.get(i).isEnabled()) {
                            keyCallbacks.get(i).invoke(window, key, scancode, action, mods);
                        }
                    }
                }

            }
        };
        glfwSetKeyCallback(window, keyCallback);

    }

    //update the frame and mouse data, called each frame by the main thread
    public void refresh() {

        glfwSwapBuffers(window); // swap the color buffers

        glfwPollEvents();

        glfwGetCursorPos(window, mouseXBuffer, mouseYBuffer);
        mouseX = (float)mouseXBuffer.get(0);
        mouseY = (float)mouseYBuffer.get(0);
    }

    public void destroy() {
        // Release window and window callbacks
        glfwDestroyWindow(window);
        exitCallback.release();

        // Terminate GLFW and release the GLFWerrorfun
        glfwTerminate();
        errorCallback.release();

    }

    public float getMouseX() {
        return mouseX;
    }
    public float getMouseY() {
        return mouseY;
    }
    
    public long getWindow() {
        return window;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void addKeyCallback(KeyCallback call) {
        keyCallbacks.add(call);
    }

    public void removeKeyCallback(KeyCallback call) {
        keyCallbacks.remove(call);
    }

    public void bindCursor() {
        glfwSetCursorPos(window, 0, 0);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }
    public void unbindCursor() {
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    public int getResX() {
        return width;
    }
    public int getResY() {
        return height;
    }
    
}
