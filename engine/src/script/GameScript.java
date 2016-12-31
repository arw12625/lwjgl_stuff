package script;

import game.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import update.Updateable;

/**
 *
 * @author Andy
 * 
 * representation of a script
 * all updates added in the script are gathered in a list to be run as one
 * 
 */
public class GameScript extends Component implements Updateable {

    private final String name;
    private final String script;
    private Object scriptObject;
    private Map<Class, Object> interfaces;
    private List<Updateable> updates;
    
    protected GameScript(Component parent, String name, String script) {
        super(parent);
        this.name = name;
        this.script = script;
        interfaces = new HashMap<>();
        updates = new ArrayList<>();
    }

    protected void setScriptObject(Object obj) {
        this.scriptObject = obj;
    }
    
    
    public String getScript() {
        return script;
    }
    
    public String getName() {
        return name;
    }
    
    protected Map<Class, Object> getInterfaces() {
        return interfaces;
    }
    
    public Object getScriptObject() {
        return scriptObject;
    }
    
    public void addUpdate(Updateable u) {
        updates.add(u);
    }

    @Override
    public void update(int delta) {
        for(Updateable u : updates) {
            u.update(delta);
        }
    }
    
}
