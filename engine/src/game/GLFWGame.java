package game;

import io.GLFWManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import resource.ResourceManager;
import update.UpdateManager;
/**
 *
 * @author Andrew_2
 */
public class GLFWGame extends Game {

    private final GLFWManager glfwManager;
    private final UpdateManager updateManager;
    private final ResourceManager resourceManager;
    private Thread updateThread;
    private Thread resourceThread;
    
    private boolean requestEnd;
    
    public GLFWGame(GameStateManager gameStateManager, GLFWManager glfwManager,
            UpdateManager updateManager, ResourceManager resourceManager) {
        super(gameStateManager);
        this.glfwManager = glfwManager;
        this.updateManager = updateManager;
        this.resourceManager = resourceManager;
    }
    
    @Override
    protected void engineInit() {
        
        while(!glfwManager.isInitialized()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(GLFWGame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        updateThread = new Thread(updateManager);
        updateThread.start();
        resourceThread = new Thread(resourceManager);
        resourceThread.start();
        
        
    }
    
    @Override
    protected void engineRun() {
        
        while(!requestEnd) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(GLFWGame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    @Override
    protected void engineRelease() {
        updateManager.release();
        resourceManager.release();
        
        glfwManager.release();
    }
    
    @Override
    public void end() {
        this.requestEnd = true;
    }
    
    //must be called from main application thread
    public void startGLFW() {
        glfwManager.run();
    }
    
    public GLFWManager getGLFWManager() {
        return glfwManager;
    }

    public UpdateManager getUpdateManager() {
        return updateManager;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }
}
