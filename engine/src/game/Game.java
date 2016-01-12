package game;

import io.GLFWManager;
import graphics.RenderManager;
import resource.ResourceManager;
import script.ScriptManager;
import update.UpdateManager;

/**
 *
 * @author Andrew_2
 * 
 * the base class for the engine
 * Game contains references to the core singleton managers
 * Game must run in the main thread that has an opengl context
 * Game is a singleton for now to allow possible inheritance
 * 
 */
public class Game {

    public GameObjectManager gameOjbectManager;
    public GLFWManager glfwManager;
    public UpdateManager updateManager;
    public RenderManager renderManager;
    public ResourceManager resourceManager;
    public ScriptManager scriptManager;
    
    private boolean requestQuit = false;
    private boolean initializing = true;

    private static Game instance = null;
    public static Game getInstance() {
        if(instance == null) {
            instance = new Game();
        }
        return instance;
    }
    
    private Game() {
    }

    //initialize the core managers in the specific order required
    public void create() {
        this.gameOjbectManager = GameObjectManager.getInstance();
        this.glfwManager = GLFWManager.getInstance();
        this.updateManager = UpdateManager.getInstance();
        updateManager.start();
        this.resourceManager = ResourceManager.getInstance();
        resourceManager.start();
        this.renderManager = RenderManager.getInstance();
        this.scriptManager = ScriptManager.getInstance();
    }
    
    public void run() {
        initializing = false;
        while(running()) {
            renderManager.render();
            glfwManager.refresh();
        }
        close();
    }

    private void close() {
        resourceManager.destroy();
        updateManager.destroy();
        glfwManager.destroy();
        System.exit(0);
    }
    
    //any object may exit the game via this method
    public void requestQuit() {
        requestQuit = true;
    }
    
    
    public boolean running() {
        return !requestQuit;
    }
    public boolean initializing() {
        return initializing;
    }
}
