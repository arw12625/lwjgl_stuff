package test;

import game.Game;
import game.GameObject;
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
public class GameTest extends Thread {
    
    Game game;

    public static void main(String[] args) {
        GameTest gt = new GameTest();
        gt.start();
        
        Game.getInstance().start();
        
    }
    
    public GameTest() {
        this.game = Game.getInstance();
    }
    
    @Override
    public void run() {
        while(!game.running()) {
            Thread.yield();
        }
        
        GameObject scene = new GameObject(game);
        GameScript load = ScriptManager.getInstance().loadScript(scene, "game_scripts/loading.js");
        GameScript print = ScriptManager.getInstance().loadScript(scene, "game_scripts/print_test.js");
        
        while(game.running()) {
            Thread.yield();
        }
        System.out.println("GameTest finished");
    }
    
}
