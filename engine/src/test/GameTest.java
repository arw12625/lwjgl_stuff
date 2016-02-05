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
        
        Updateable u = new Updateable() {

            boolean finished;

            @Override
            public void update(int delta) {
                Component scene = g;
                ScriptManager.getInstance().loadScript(scene, "game_scripts/game.js");
                ScriptManager.getInstance().loadScript(scene, "game_scripts/viewpoint.js");
                //ScriptManager.getInstance().loadScript(scene, "game_scripts/sound_test.js");
                finished = true;
            }

            @Override
            public boolean isDestroyed() {
                return finished;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        };
        UpdateManager.getInstance().add(u);
        g.run();
    }
}
