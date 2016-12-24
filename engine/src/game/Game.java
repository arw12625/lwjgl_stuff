package game;

import java.util.Stack;
/**
 *
 * @author Andrew_2
 */
public abstract class Game extends Component implements Runnable {

    GameStateManager stateManager;
    
    public Game(GameStateManager stateManager) {
        super(null);
        this.stateManager = stateManager;
        stateManager.pushState(new EngineConstruct());
    }
   
    @Override
    public final void run() {
        stateManager.setState(new EngineInit());
        engineInit();
        stateManager.setState(new EngineRun());
        engineRun();
        stateManager.setState(new EngineRelease());
        engineRelease();
    }
    
    public abstract void end();

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
 
    public GameStateManager getStateStack() {
        return stateManager;
    }
    
    public static boolean isEngineInit(GameState state) {
        return state instanceof EngineInit;
    }
    
    public static boolean isEngineRunning(GameState state) {
        return state instanceof EngineRun;
    }
    
    public static boolean isEngineReleased(GameState state) {
        return state instanceof EngineRelease;
    }
    
}
