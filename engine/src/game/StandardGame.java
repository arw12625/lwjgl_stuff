package game;

import graphics.RenderManager;
import io.GLFWManager;
import io.KeyCallback;
import io.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import org.lwjgl.glfw.GLFWKeyCallback;
import resource.ResourceManager;
import script.ScriptManager;
import sound.SoundManager;
import update.Action;
import update.UpdateManager;

/**
 *
 * @author Andrew_2
 */
public class StandardGame extends GLFWGame {

    private Window window;
    private RenderManager renderManager;
    private ScriptManager scriptManager;
    private SoundManager soundManager;
    
    private Thread renderThread;
    
    public static final int DEFAULT_WINDOW_WIDTH = 640;
    public static final int DEFAULT_WINDOW_HEIGHT = 480;
    
    private static final Logger LOG = LoggerFactory.getLogger(StandardGame.class);

    
    public StandardGame(GameStateManager gameStateManager, GLFWManager glfwManager,
            UpdateManager updateManager, ResourceManager resourceManager,
            Window window,
            RenderManager renderManager, ScriptManager scriptManager,
            SoundManager soundManager) {
        super(gameStateManager, glfwManager, updateManager, resourceManager);
        this.window = window;
        this.renderManager = renderManager;
        this.scriptManager = scriptManager;
        this.soundManager = soundManager;
    }
    
    @Override
    protected void engineInit() {
        super.engineInit();
        
        window.initialize();
        
        renderThread = new Thread(renderManager);
        renderThread.start();
        
        soundManager.initialize();
        
        scriptManager.initialize();
        
        scriptManager.addGLobal("gameInst", this);
        scriptManager.addGLobal("renderManager", renderManager);
        scriptManager.addGLobal("window", window);
        scriptManager.addGLobal("resourceManager", getResourceManager());
        scriptManager.addGLobal("scriptManager", scriptManager);
        scriptManager.addGLobal("soundManager", soundManager);
        scriptManager.addGLobal("updateManager", getUpdateManager());
        scriptManager.addGLobal("glfwManager", getGLFWManager());
        
        while(!renderManager.isInitialized()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                LOG.error("{}", ex);
                }
        }
        
        
        
        Action exitAction = new Action() {

            @Override
            public void act(Object... args) {
                requestEnd();
            }
            
        };
        
        KeyCallback exitKeyCallback = new KeyCallback() {
            
            @Override
            public void invokeKey(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                    exitAction.act();
                }
            }
        };
        window.addExitCallback(exitAction);
        window.addKeyCallback(exitKeyCallback);
        
       
    }
    
    @Override
    protected void engineRelease() {
        scriptManager.release();
        soundManager.release();
        renderManager.release();//wait for release in function
        
       super.engineRelease();
    }
    
    public RenderManager getRenderManager() {
        return renderManager;
    }
    public ScriptManager getScriptManager() {
        return scriptManager;
    }
    public SoundManager getSoundManager() {
        return soundManager;
    }
    
    public static StandardGame createStandardGame() {
        LOG.info("Creating Standard Game");
        GameStateManager gameStateManager = new GameStateManager();
        GLFWManager glfwManager = new GLFWManager();
        UpdateManager updateManager = new UpdateManager();
        ResourceManager resourceManager = new ResourceManager();
        
        ScriptManager scriptManager = new ScriptManager(updateManager, resourceManager);
        
        Window window = new Window("test", DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT, glfwManager);
        RenderManager renderManager = new RenderManager(window);
        SoundManager soundManager = new SoundManager();
        
        LOG.info("Standard Game managers created");
        
        StandardGame game = new StandardGame(gameStateManager, glfwManager, updateManager, resourceManager, window, renderManager, scriptManager, soundManager);
        Thread gameThread = new Thread(game);
        gameThread.start();
        
        LOG.info("Standard Game started");
        
        return game;
    }
}
