package game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
/**
 *
 * @author Andrew_2
 */
public abstract class Game extends Component implements Runnable {

    GameStateManager stateManager;
    
    private static final Logger LOG = LoggerFactory.getLogger(Game.class);
    public static final Marker threadMarker = MarkerFactory.getMarker("thread");
    
    public Game(GameStateManager stateManager) {
        super(null);
        LOG.info("Game constructor entered");
        this.stateManager = stateManager;
        stateManager.pushState(new EngineConstruct());
        LOG.info("Game constructor exited");
    }
   
    @Override
    public final void run() {
        LOG.info(Game.threadMarker, "Game");
        LOG.info("Engine Started");
        engineInit_();
        engineRun_();
        engineRelease_();
        LOG.info("Engine Ended");
    }
    
    public final void requestEnd() {
        LOG.info("Engine end requested");
        endRequested();
    }
    
    public abstract void endRequested();

    private void engineInit_() {
        LOG.info("Engine Init entered");
        stateManager.setState(new EngineInit());
        engineInit();
        LOG.info("Engine Init exited");
    }
    
    private void engineRun_() {
        LOG.info("Engine Run Entered");
        stateManager.setState(new EngineRun());
        engineRun();
        LOG.info("Engine Run Exited");
    }
    
    private void engineRelease_() {
        LOG.info("Engine Release Entered");
        stateManager.setState(new EngineRelease());
        engineRelease();
        stateManager.setState(new EngineExited());
        LOG.info("Engine Release Exited");
    }
    
    protected abstract void engineInit();
    protected abstract void engineRun();
    protected abstract void engineRelease();
    
    //EngineConstruct is the state representing the initial state before starting
    private final static class EngineConstruct implements GameState{}
    //EngineInit is the state representing the initialization of integral engine features
    protected static class EngineInit implements GameState{}
    //EngineRun is the state representing the engine in its normal operating state
    //All game specific states should derive from this class
    public static class EngineRun implements GameState{}
    //EngineEnd is the state representing the shutdown of the integral engine features
    protected static class EngineRelease implements GameState{}
    //EngineExited final state after release
    protected static class EngineExited implements GameState{}
 
    public GameStateManager getStateStack() {
        return stateManager;
    }
    
    public static boolean isEngineInit(GameState state) {
        return state instanceof EngineInit;
    }
    
    public static boolean isEngineRunning(GameState state) {
        return state instanceof EngineRun;
    }
    
    public static boolean isEngineRelease(GameState state) {
        return state instanceof EngineRelease;
    }
    public static boolean isEngineExited(GameState state) {
        return state instanceof EngineExited;
    }
    
}
