package io;

import game.Game;
import java.nio.DoubleBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.system.MemoryUtil.NULL;
import update.Action;

/**
 *
 * @author Andrew_2
 */
public class Window {

    private boolean windowCreated;
    private boolean contextCreated;
    private long handle;

    private GLFWWindowCloseCallback exitCallback;

    private String title;
    private int width, height;

    private GLFWKeyCallback glfwKeyCallback;
    private GLFWMouseButtonCallback glfwMouseButtonCallback;

    private List<KeyCallback> keyCallbacks;
    private List<MouseButtonCallback> mouseButtonCallbacks;

    private TextInput defaultTextInput;

    private DoubleBuffer mouseXBuffer, mouseYBuffer;
    private float mouseX, mouseY;
    private float dMouseX, dMouseY;
    private boolean isDestroyed;
    private boolean toRelease;
    private long numRefreshes;

    private List<Action> exitCallbackActions;

    protected Window(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
        exitCallbackActions = new CopyOnWriteArrayList<>();
    }

    protected void initialize() {
        initializeWindow();
        initializeMouse();
        initializeKeyBoard();
        windowCreated = true;
    }

    private void initializeWindow() {

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable

        // Create the window
        handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (handle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
                handle,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2
        );

        // Make the window visible
        glfwShowWindow(handle);

        exitCallback = new GLFWWindowCloseCallback() {

            @Override
            public void invoke(long windowE) {
                if (windowE == handle) {
                    for (Action a : exitCallbackActions) {
                        a.act(Window.this);
                    }
                }
            }
        };
        glfwSetWindowCloseCallback(handle, exitCallback);
    }

    private void initializeMouse() {

        mouseButtonCallbacks = new CopyOnWriteArrayList<>();
        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {

            @Override
            public void invoke(long window, int button, int action, int mods) {
                for (MouseButtonCallback mbcb : mouseButtonCallbacks) {
                    if (mbcb.isDestroyed()) {
                        mouseButtonCallbacks.remove(mbcb);
                    } else if (mbcb.isEnabled()) {
                        mbcb.invoke(window, button, action, mods);
                    }
                }
            }
        };

        mouseXBuffer = BufferUtils.createDoubleBuffer(1);
        mouseYBuffer = BufferUtils.createDoubleBuffer(1);

    }

    private void initializeKeyBoard() {
        keyCallbacks = new CopyOnWriteArrayList<>();
        //pressing escape exits the game by default
        glfwKeyCallback = new GLFWKeyCallback() {

            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {

                for (KeyCallback kcb : keyCallbacks) {
                    if (kcb.isDestroyed()) {
                        keyCallbacks.remove(kcb);
                    } else if (kcb.isEnabled()) {
                        kcb.invoke(window, key, scancode, action, mods);
                    }
                }

            }
        };
        glfwSetKeyCallback(handle, glfwKeyCallback);

        defaultTextInput = new TextInput(null);
        addKeyCallback(defaultTextInput);
    }

    public void refresh() {

        glfwGetCursorPos(handle, mouseXBuffer, mouseYBuffer);
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

    public void bindGLContext() {
        if (contextCreated) {
            System.err.println("Context already bound for this window");
        } else {
            // Make the OpenGL context current
            glfwMakeContextCurrent(handle);
            // This line is critical for LWJGL's interoperation with GLFW's
            // OpenGL context, or any context that is managed externally.
            // LWJGL detects the context that is current in the current thread,
            // creates the ContextCapabilities instance and makes the OpenGL
            // bindings available for use.
            //GLContext.createFromCurrent();
            GL.createCapabilities();

            // Enable v-sync
            glfwSwapInterval(1);
            contextCreated = true;
        }
    }

    protected void destroy() {
        Callbacks.glfwReleaseCallbacks(handle);
        GLFW.glfwDestroyWindow(handle);
        isDestroyed = true;
    }

    public void swapBuffers() {

        glfwSwapBuffers(handle); // swap the color buffers
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDimension(int width, int height) {
        //this.width = width;
        //this.height = height;
        System.err.println("THIS FUNCTION NOT YET SUPPORTED");
    }

    public boolean isCreated() {
        return windowCreated;
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
        glfwSetCursorPos(handle, 0, 0);
        glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public void unbindCursor() {
        glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    private void closeWindow() {
        glfwSetWindowShouldClose(handle, 1);
    }

    public void release() {
        toRelease = true;
        while (!isDestroyed) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public TextInput getDefaultTextInput() {
        return defaultTextInput;
    }

    public void addExitCallback(Action a) {
        exitCallbackActions.add(a);
    }

    public boolean toRelease() {
        return toRelease;
    }

}
