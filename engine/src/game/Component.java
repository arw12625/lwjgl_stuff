package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import update.Action;

/**
 *
 * @author Andrew_2
 *
 * The base class for objects in the game Components form trees with each having
 * one parent and children
 *
 * Note that the parent is allowed to be null, however this will not allow
 * certain features such as dispatching events object wide
 */
public abstract class Component {

    private Component parent;
    private final List<Component> children;
    private final Map<String, List<Action>> eventHandler;
    private boolean isEnabled = true;
    private boolean requestRelease = false;
    private boolean isReleased = false;

    private static final Logger LOG = LoggerFactory.getLogger(Component.class);

    public Component() {
        eventHandler = new ConcurrentHashMap<>();
        children = new CopyOnWriteArrayList<>();
    }

    private void setParent(Component parent) {

        if (parent != null) {
            if (this.parent == null) {
                this.parent = parent;
                setEnabled(parent.isEnabled());
            } else {
                LOG.warn("Cannot set Component parent ");
            }
        } else {
            LOG.warn("Cannot set component parent to null.");
        }
    }

    public final List<Component> getChildren() {
        return new ArrayList<>(children);
    }

    public final Component getChild(String name) {
        for (Component c : children) {
            if (c.getClass().getSimpleName().equals(name)) {
                return c;
            }
        }
        return null;
    }

    public final void addChild(Component c) {
        children.add(c);
        c.setParent(this);
    }

    private void removeChild(Component c) {
        children.remove(c);
    }

    public final Component getParent() {
        return parent;
    }

    protected void releaseComponent(){}
    
    public final void release() {
        requestRelease = true;
        dispatchLocal("release");
        releaseComponent();
        parent.removeChild(this);
    }

    public boolean requestRelease() {
        return requestRelease;
    }
    
    protected final void setReleased() {
        this.isReleased = true;
    }
    
    public boolean isReleased() {
        return isReleased;
    }

    public final void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        dispatchLocal("enable");
        for (Component child : children) {
            child.setEnabled(enabled);
        }
    }

    public final boolean isEnabled() {
        return isEnabled;
    }

    public final void addDispatch(String name, Action action) {
        if (eventHandler.get(name) == null) {
            eventHandler.put(name, new CopyOnWriteArrayList<>());
        }
        eventHandler.get(name).add(action);
    }

    public final void removeDispatch(String name, Action action) {
        List<Action> actions = eventHandler.get(name);
        if (actions != null) {
            actions.remove(action);
        }
    }

    //receive an event with name and arguments
    public final void dispatchLocal(String name, Object... args) {
        List<Action> actions = eventHandler.get(name);
        if (actions != null) {
            for (Action a : eventHandler.get(name)) {
                a.act(args);
            }
        }
    }
    //receive an event with name and arguments
    public final void dispatch(String name, Object... args) {
        dispatchLocal(name, args);
        for (Component child : children) {
            child.dispatch(name, args);
        }
    }
}
