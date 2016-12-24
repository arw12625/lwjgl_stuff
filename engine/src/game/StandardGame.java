package game;

import graphics.RenderManager;
import static graphics.RenderManager.DEFAULT_WINDOW_HEIGHT;
import static graphics.RenderManager.DEFAULT_WINDOW_WIDTH;
import io.GLFWManager;
import io.KeyCallback;
import io.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private RenderManager renderManager;
    private ScriptManager scriptManager;
    private SoundManager soundManager;
    
    private Thread renderThread;
    
    public StandardGame(GameStateManager gameStateManager, GLFWManager glfwManager,
            UpdateManager updateManager, ResourceManager resourceManager,
            RenderManager renderManager, ScriptManager scriptManager,
            SoundManager soundManager) {
        super(gameStateManager, glfwManager, updateManager, resourceManager);
        this.renderManager = renderManager;
        this.scriptManager = scriptManager;
        this.soundManager = soundManager;
    }
    
    @Override
    protected void engineInit() {
        super.engineInit();
        
        renderThread = new Thread(renderManager);
        renderThread.start();
        Window w = renderManager.getWindow();
        
        soundManager.initialize();
        
        scriptManager.initialize();
        
        scriptManager.addGLobal("gameInst", this);
        scriptManager.addGLobal("renderManager", renderManager);
        scriptManager.addGLobal("window", w);
        scriptManager.addGLobal("resourceManager", getResourceManager());
        scriptManager.addGLobal("scriptManager", scriptManager);
        scriptManager.addGLobal("soundManager", soundManager);
        scriptManager.addGLobal("updateManager", getUpdateManager());
        scriptManager.addGLobal("glfwManager", getGLFWManager());
        
        while(!renderManager.isInitialized()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(StandardGame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
        
        Action exitAction = new Action() {

            @Override
            public void act(Object... args) {
                end();
            }
            
        };
        KeyCallback exitKeyCallback = new KeyCallback(this) {
            
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                    exitAction.act();
                }
            }
        };
        w.addExitCallback(exitAction);
        w.addKeyCallback(exitKeyCallback);
        
        
        
        
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
        GameStateManager gameStateManager = new GameStateManager();
        GLFWManager glfwManager = new GLFWManager();
        UpdateManager updateManager = new UpdateManager();
        ResourceManager resourceManager = new ResourceManager();
        
        ScriptManager scriptManager = new ScriptManager(updateManager, resourceManager);
        
        Window window = glfwManager.createWindow("test", 
                DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
        RenderManager renderManager = new RenderManager(window);
        SoundManager soundManager = new SoundManager();
        
        StandardGame game = new StandardGame(gameStateManager, glfwManager, updateManager, resourceManager, renderManager, scriptManager, soundManager);
        Thread gameThread = new Thread(game);
        gameThread.start();
        return game;
    }
}
