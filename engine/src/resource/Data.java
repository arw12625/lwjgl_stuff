package resource;

/**
 *
 * @author Andrew_2
 * 
 * A representation of game data or assets used in conjuction with Resource
 * Data need not be loaded from a file, but must be able to be loaded this way
 */
public abstract class Data {
    
    public abstract void load(String path);
}
