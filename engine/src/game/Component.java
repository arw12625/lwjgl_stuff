package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import update.Action;

/**
 *
 * @author Andrew_2
 *
 * The base class for objects in the game
 * Components form trees with each having one parent and children
 *
 * Note that the parent is allowed to be null, however this will not allow
 * certain features such as dispatching events object wide
 */
public abstract class Component {

    private final Component parent;
    private final List<Component> children;
    private final Map<String, List<Action>> eventHandler;
    private boolean enabled = true;
    private boolean destroyed = false;

    public Component(Component parent) {
        this.parent = parent;
        eventHandler = new HashMap<>();
        children = new CopyOnWriteArrayList<>();
        if (parent != null) {
            parent.addChild(this);
        }
        this.dispatch("added");
    }

    public List<Component> getChildren() {
        return children;
    }

    protected void addChild(Component c) {
        children.add(c);
    }

    protected void removeChild(Component c) {
        children.remove(c);
    }

    public Component getParent() {
        return parent;
    }

    protected void destroyInternal() {
        destroyed = true;
        for(Component child : children) {
            child.destroyInternal();
        }
    }

    public void destroy() {
        dispatch("removed");
        destroyInternal();
        parent.removeChild(this);
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void enable(boolean enabled) {
        this.enabled = enabled;
        for(Component child : children) {
            child.enable(enabled);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void addDispatch(String name, Action action) {
        if (eventHandler.get(name) == null) {
            eventHandler.put(name, new CopyOnWriteArrayList<>());
        }
        eventHandler.get(name).add(action);
    }
    
    public void removeDispatch(String name, Action action) {
        List<Action> actions = eventHandler.get(name);
        if(actions != null) {
            actions.remove(action);
        }
    }

    public Map<String, List<Action>> getDispatch() {
        return eventHandler;
    }

    //receive an event with name and arguments
    public final void dispatch(String name, Object... args) {
        List<Action> actions = eventHandler.get(name);
        if (actions != null) {
            for (Action a : eventHandler.get(name)) {
                a.act(args);
            }
        }
        for (Component child : children) {
            child.dispatch(name, args);
        }
    }

    public Component getChild(String name) {
        for (Component c : children) {
            if (c.getClass().getSimpleName().equals(name)) {
                return c;
            }
        }
        return null;
    }
}
