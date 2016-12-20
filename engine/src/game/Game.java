package game;

import graphics.RenderManager;
import io.GLFWManager;
import resource.ResourceManager;
import script.ScriptManager;
import sound.SoundManager;
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
 * Game also serves as the root component
 * 
 */
public class Game extends Component {

    public GLFWManager glfwManager;
    public UpdateManager updateManager;
    public ResourceManager resourceManager;
    public ScriptManager scriptManager;
    public SoundManager soundManager;
    public RenderManager renderManager;
    
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
        super(null);
    }
    
    public void start() {
        create();
        run();
        //destroy();
    }

    //initialize the core managers in the specific order required
    public void create() {
        this.glfwManager = GLFWManager.getInstance();
        this.updateManager = UpdateManager.getInstance();
        updateManager.start();
        this.resourceManager = ResourceManager.getInstance();
        resourceManager.start();
        this.renderManager = RenderManager.getInstance();
        renderManager.start();
        this.scriptManager = ScriptManager.getInstance();
        this.soundManager = SoundManager.getInstance();
    }
    
    public void run() {
        initializing = false;
        while(running()) {
            glfwManager.refresh();
        }
        close();
    }

    @Override
    public void destroy() {
        throw new UnsupportedOperationException("Destroy game with request quit");
    }
    
    private void close() {
        soundManager.destroy();
        resourceManager.destroy();
        updateManager.destroy();
        glfwManager.destroy();
        this.destroyInternal();
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
