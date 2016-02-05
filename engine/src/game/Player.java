package game;

import update.Updateable;

/**
 *
 * @author Andy
 * 
 * A class to represent the human player in the engine
 */
public abstract class Player extends Component implements Updateable {

    public Player(Component parent) {
        super(parent);
    }
}
