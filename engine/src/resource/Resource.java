package resource;

/**
 *
 * @author Andy
 * 
 * A wrapper of a Data object that interfaces with resource manager
 * Handles paths and loading
 * 
 */
public class Resource<T extends Data> {
    
    private boolean loaded = false;
    private T data;
    private String path;
    
    //Resource must be constructed with the path of the data
    //as well as a data to be loaded or written
    protected Resource(String path, T data) {
        this.path = path;
        this.data = data;
    }
    
    public String getPath() {
        return path;
    }
    
    protected void loadData(ResourceManager resourceManager) {
        data.load(path, resourceManager);
        loaded = true;
    }
    
    
    public boolean isLoaded(){
        return loaded;
    }
    
    public T getData() {
        return data;
    }
    
}
