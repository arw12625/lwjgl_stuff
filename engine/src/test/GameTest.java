package test;

import game.*;
import script.ScriptManager;
import update.UpdateManager;
import update.Updateable;

/**
 *
 * @author Andy
 * 
 * The main test class for now
 */
public class GameTest {

    public static void main(String[] args) {

        final Game g = Game.getInstance();
        g.create();
        
        GameObject scene = new GameObject(g);
        ScriptManager.getInstance().loadScript(scene, "game_scripts/loading.js");
     
        g.run();
    }
}
