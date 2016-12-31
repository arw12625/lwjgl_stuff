package resource;

/**
 *
 * @author Andrew_2
 * 
 * A representation of game data or assets used in conjuction with Resource
 * Data need not be loaded from a file, but should be able to be loaded and
 * written this way
 */
public interface Data {
    
    public void load(String path, ResourceManager resourceManager);
    public void write(String path, ResourceManager resourceManager);
    public default boolean isValid() {
        return true;
    }
}
