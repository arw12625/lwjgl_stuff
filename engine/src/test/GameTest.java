package test;

import game.Game;
import game.GameObject;
import game.StandardGame;
import graphics.RenderLayer;
import graphics.visual.SkyBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.collada.ColladaModel;
import script.GameScript;
import script.ScriptManager;

/**
 *
 * @author Andy
 * 
 * The main test class for now
 */
public class GameTest implements Runnable {
    
    private StandardGame game;
    
    static final Logger LOG = LoggerFactory.getLogger(GameTest.class);

    public static void main(String[] args) {
        
        LOG.info("GameTest start");
        
        StandardGame game = StandardGame.createStandardGame();
        
        GameTest gt = new GameTest(game);
        Thread testThread = new Thread(gt);
        testThread.start();
        
        game.startGLFW();
        
    }
    
    public GameTest(StandardGame game) {
        this.game = game;
    }
    
    @Override
    public void run() {
        
        LOG.info("GameTest thread start");
        while(!Game.isEngineRunning(game.getStateStack().getState())) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                LOG.error("{}", ex);
            }
        
        }
        ScriptManager scriptManager = game.getScriptManager();
        
        
        GameScript test = scriptManager.loadScript("game_scripts/test.js");
        
        LOG.info("GameTest scripts added");
        
        while(!Game.isEngineExited(game.getStateStack().getState())) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                LOG.error("{}", ex);
            }
        }
        
        LOG.info("GameTest thread finished");
    }
    
}
