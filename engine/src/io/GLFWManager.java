package io;

import game.Game;
import graphics.ui.Console;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.lwjgl.BufferUtils;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 *
 * @author Andrew_2
 *
 * The interface with GLFW GLFW interfaces with opengl and IO, handles opengl
 * errors GLFWManager manages the display, key, and mouse functions Events
 * handled with callbacks MouseData also available with getMouseX() and
 * getMouseY()
 *
 */
public class GLFWManager {

    private GLFWErrorCallback errorCallback;
    private GLFWWindowCloseCallback exitCallback;
    private GLFWKeyCallback glfwKeyCallback;
    private GLFWMouseButtonCallback glfwMouseButtonCallback;

    private List<KeyCallback> keyCallbacks;
    private List<MouseButtonCallback> mouseButtonCallbacks;
    
    private TextInput defaultTextInput;

    private DoubleBuffer mouseXBuffer, mouseYBuffer;
    private float mouseX, mouseY;
    private float dMouseX, dMouseY;

    // The window handle
    private long window;

    private String title = "test";
    private int width = 640, height = 480;

    private long numRefreshes;

    private static GLFWManager instance;

    public static GLFWManager getInstance() {
        if (instance == null) {
            instance = new GLFWManager();
        }
        return instance;
    }

    private GLFWManager() {
        setupWindow();
        
        numRefreshes = 0;
        mouseButtonCallbacks =  new CopyOnWriteArrayList<>();
        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {

            @Override
            public void invoke(long window, int button, int action, int mods) {
                for(MouseButtonCallback mbcb : mouseButtonCallbacks) {
                    if(mbcb.isDestroyed()) {
                        mouseButtonCallbacks.remove(mbcb);
                    } else if(mbcb.isEnabled() ) {
                        mbcb.invoke(window, button, action, mods);
                    }
                }
            }
        };

        mouseXBuffer = BufferUtils.createDoubleBuffer(1);
        mouseYBuffer = BufferUtils.createDoubleBuffer(1);

        
        
        keyCallbacks = new CopyOnWriteArrayList<>();
        //pressing escape exits the game by default
        glfwKeyCallback = new GLFWKeyCallback() {

            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                    Game.getInstance().requestQuit();
                } else {
                    for(KeyCallback kcb : keyCallbacks) {
                        if(kcb.isDestroyed()) {
                            keyCallbacks.remove(kcb);
                        } else if(kcb.isEnabled()) {
                            kcb.invoke(window, key, scancode, action, mods);
                        }
                    }
                }

            }
        };
        glfwSetKeyCallback(window, glfwKeyCallback);
        
        defaultTextInput = new TextInput(null);
        addKeyCallback(defaultTextInput);
        
    }
    
    //Initialize the GLFWWindow
    private void setupWindow() {
        
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
    }

    //update the frame and mouse data, called each frame by the main thread
    public void refresh() {

        glfwSwapBuffers(window); // swap the color buffers

        glfwPollEvents();

        glfwGetCursorPos(window, mouseXBuffer, mouseYBuffer);
        //ignore mouse input for a number of frames
        float oldX = mouseX;
        float oldY = mouseY;
        mouseX = (float) mouseXBuffer.get(0);
        mouseY = (float) mouseYBuffer.get(0);
        if (numRefreshes > 20) {
            dMouseX = mouseX - oldX;
            dMouseY = mouseY - oldY;
        }

        numRefreshes++;
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

    public float getDMouseX() {
        return dMouseX;
    }

    public float getDMouseY() {
        return dMouseY;
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
    public void addMouseButtonCallback(MouseButtonCallback call) {
        mouseButtonCallbacks.add(call);
    }

    public void removeMouseButton(MouseButtonCallback call) {
        mouseButtonCallbacks.remove(call);
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

    public TextInput getDefaultTextInput() {
        return defaultTextInput;
    }

}
