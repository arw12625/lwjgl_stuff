package resource;

import game.Game;
import game.GameStateManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andy
 * 
 * ResourceManager handles the reading and writing of files in the form of resources
 * 
 * ResourceManager runs in its own thread
 * this allows the queueing of resources to be loaded without blocking the update thread
 * data is loaded through either the loadResource or queueResource methods
 */
public class ResourceManager implements Runnable {

    private Queue<Resource> queuedResources;
    private Map<String, Resource> resources;
    private boolean toRelease, isReleased;
    
    private static final Logger LOG = LoggerFactory.getLogger(ResourceManager.class);
    
    public ResourceManager() {
        LOG.info("ResourceManager constructor entered");
        queuedResources = new ConcurrentLinkedQueue<>();
        resources = new HashMap<>();
        LOG.info("ResourceManager constructor exited");
        
    }
    
    public void release() {
        LOG.info("ResourceManager release entered");
        toRelease = true;
        while(!isReleased) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                LOG.error("{}", ex);
            }
        }
        /*queuedResources.clear();
        resources.clear();*/
        LOG.info("ResourceManager release exited");
    }

    //this method will return a resource with the appropriate data from the path
    //data must be of the appropriate type
    //note that it is not required that data will contain the loaded data
    //if the resource is already loaded, this will return the loaded resource and data will remain unchanged
    public <T extends Data> Resource<T> loadResource(String path, T data) {
        return loadResource(path, false, data);
    }

    //reload forces the manager to load the file even if it is already loaded
    public <T extends Data> Resource<T> loadResource(String path, boolean reload, T data) {
        return loadResource(new Resource(path, data), reload);
    }
    
    private <T extends Data> Resource<T> loadResource(Resource<T> r, boolean reload) {
        if (!reload) {
            Resource found = resources.get(r.getPath());
            if (found != null && found.isLoaded()) {
                return found;
            }
        }
        r.loadData(this);
        resources.put(r.getPath(), r);
        LOG.debug("{} loaded from {}", r.getData().getClass().getName(), r.getPath());
        return r;
    }

    @Override
    public void run() {
        LOG.info(Game.threadMarker, "Resource");
        LOG.info("ResourceManager run");
        while (!toRelease) {
            Resource r;
            while ((r = queuedResources.poll()) != null) {
                loadResource(r, true);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                LOG.error("{}", ex);
            }
        }
        isReleased = true;
    }

    //queue a resource for loading, similar to loadResource
    public <T extends Data> Resource<T> queueResource(String path, boolean reload, T data) {
        Resource<T> r;
        if (!reload) {
            r = resources.get(path);
            if (r != null) {
                return r;
            }
        }
        r = new Resource(path, data);
        resources.put(path, r);
        queuedResources.add(r);
        return r;
    }

    public boolean isLoading() {
        return !queuedResources.isEmpty();
    }

    public static String getResourceDirectory() {
        return "res/";
    }

    //return the file name without the file extension
    //path assumed to have directory and file extension
    public static String getFileNameNoExt(String path) {
        return path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
    }

    //return the path without parent directories
    public static String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    //return the parent directorys of the file
    public static String getDirectory(String path) {
        return path.substring(0, path.lastIndexOf("/") + 1);
    }

    //paths relative to the resource folder
    public BufferedWriter getWriter(String path) {
        try {
            return new BufferedWriter(new FileWriter(getFile(path)));
        } catch (IOException ex) {
            LOG.error("{}",ex);
        }
        return null;
    }
    //paths relative to the resource folder
    public FileInputStream getFileInputStream(String path) {
        try {
            return new FileInputStream(getResourceDirectory() + path);
        } catch (IOException ex) {
            LOG.error("{}", ex);
        }
        return null;
    }
    //paths relative to the resource folder
    public FileOutputStream getFileOutputStream(String path) {
        try {
            return new FileOutputStream(getResourceDirectory() + path);
        } catch (IOException ex) {
            LOG.error("{}", ex);
        }
        return null;
    }
    //paths relative to the resource folder
    public File getFile(String path) {
        return new File(getResourceDirectory() + path);
    }

    //paths relative to the resource folder
    public BufferedReader getReader(String path) {
        try {
            return new BufferedReader(new FileReader(getResourceDirectory() + path));
        } catch (FileNotFoundException ex) {
            LOG.error("{}",ex);
        }
        return null;
    }

    //paths relative to the resource folder
    public ByteBuffer getBuffer(String path) {
        Resource<BufferData> bufferRes = loadResource(path, new BufferData());
        return bufferRes.getData().getData();
    }

    public ByteBuffer loadBuffer(String path) {
        return loadResource(path, true, new BufferData()).getData().getData();
    }

    
    
}
