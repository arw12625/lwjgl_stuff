package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import update.Action;

/**
 *
 * @author Andrew_2
 *
 * Children of GameObject Any behavior or property of an object should derive
 * from this class Component dispatches event information
 * 
 * Note that the parent is allowed to be null, however this will not allow
 * certain features such as dispatching events object wide
 */
public abstract class Component {

    GameObject parent;
    Map<String, List<Action>> eventHandler;
    private boolean enabled = true;
    private boolean destroyed = false;

    public Component(GameObject parent) {
        this.parent = parent;
        eventHandler = new HashMap<>();
        if (parent != null) {
            parent.addComponent(this);
        }
    }

    public GameObject getParent() {
        return parent;
    }

    protected void destroy() {
        destroyed = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void enable(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void addDispatch(String name, Action action) {
        if (eventHandler.get(name) == null) {
            eventHandler.put(name, new ArrayList<>());
        }
        eventHandler.get(name).add(action);
    }
    
    
    public Map<String, List<Action>> getDispatch() {
        return eventHandler;
    }

    //receive an event with name and arguments
    public void dispatch(String name, Object... args) {
        List<Action> actions = eventHandler.get(name);
        if (actions != null) {
            for (Action a : eventHandler.get(name)) {
                a.act(args);
            }
        }
    }
}
