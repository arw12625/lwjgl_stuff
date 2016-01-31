package game;

import java.util.ArrayList;

/**
 *
 * @author Andy
 */
public class GameObjectManager {

    ArrayList<GameObject> gameObjects;
    
    static GameObjectManager instance;
    
    private GameObjectManager() {
        gameObjects = new ArrayList<>();
    }
    
    public static GameObjectManager getInstance() {
        if(instance == null) {
            instance = new GameObjectManager();
        }
        return instance;
    }

    public GameObject createObject() {
        GameObject g = new GameObject();
        gameObjects.add(g);
        return g;
    }

    public void destroyObject(GameObject obj) {
        obj.destroy();
        gameObjects.remove(obj);
    }
    
    public void destroyComponent(Component c) {
        c.getParent().removeComponent(c);
    }
    
}
