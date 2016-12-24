package test;

import game.Game;
import game.GameObject;
import game.StandardGame;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import script.GameScript;
import script.ScriptManager;

/**
 *
 * @author Andy
 * 
 * The main test class for now
 */
public class GameTest implements Runnable {
    
    StandardGame game;

    public static void main(String[] args) {
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
        while(!Game.isEngineRunning(game.getStateStack().getState())) {
            Thread.yield();
        
        }
        ScriptManager scriptManager = game.getScriptManager();
        
        GameObject scene = new GameObject(game);
        GameScript load = scriptManager.loadScript(scene, "game_scripts/loading.js");
        GameScript print = scriptManager.loadScript(scene, "game_scripts/print_test.js");
        
        while(!Game.isEngineReleased(game.getStateStack().getState())) {
            Thread.yield();
        }
        System.out.println("GameTest finished");
    }
    
}
