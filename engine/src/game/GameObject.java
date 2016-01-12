package game;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andy
 * 
 * GameObject represents all objects within the game up to some interpretation
 * GameObjects should be active elements within a scene, not just vessels for information
 * GameObjects are simply a collection of components with added functionality for interconnection
 * 
 */
public class GameObject {

    List<Component> components;

    GameObject() {
        components = new ArrayList<>();
    }
    
    public List<Component> getComponents() {
        return components;
    }

    protected void addComponent(Component c) {
        components.add(c);
        c.dispatch("added");
    }
    
    protected void removeComponent(Component c) {
        c.dispatch("removed");
        components.remove(c);
        c.destroy();
    }
    
    protected void destroy() {
        List<Component> copy = new ArrayList<>(getComponents());
        for(Component c : copy) {
            removeComponent(c);
        }
    }
    
    public void dispatch(String name, Object[] args) {
        for(Component c : components) {
            if(c.isEnabled()) {
                c.dispatch(name, args);
            }
        }
    }
    
    public Component getComponent(String name) {
        for(Component c : components) {
            if(c.getClass().getSimpleName().equals(name)) {
                return c;
            }
        }
        return null;
    }
}
