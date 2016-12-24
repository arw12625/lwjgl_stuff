package script;

import game.Component;
import game.Game;
import java.util.ArrayList;
import java.util.List;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import resource.ResourceManager;
import resource.TextData;
import update.UpdateManager;
/**
 *
 * @author Andy
 * 
 * An interface with the Nashorn javascript engine
 * 
 * creates basic functionality for scripts
 */
public class ScriptManager {

    List<GameScript> scripts;
    ScriptEngine engine;
    Invocable inv;
    static ScriptManager instance;
    private GameScript[] startupScripts;
    
    private UpdateManager updateManager;
    private ResourceManager resourceManager;

    public ScriptManager(UpdateManager updateManager, ResourceManager resourceManager) {
        scripts = new ArrayList<>();
        this.updateManager = updateManager;
        this.resourceManager = resourceManager;
    }

    public void initialize() {
        
        // create a script engine manager
        ScriptEngineManager factory = new ScriptEngineManager();
        // create a Nashorn script engine
        engine = factory.getEngineByName("nashorn");
        inv = (Invocable)engine; 

        //gather core packages under core variable
        eval("core = JavaImporter(Packages.game, Packages.graphics, Packages.io, Packages.resource, Packages.script, Packages.update)");
        
        //load packages
        String[] packageNames = {"game", "geometry", "graphics", "io", "physics", "resource", "script","sound", "update", "util"};
        for (String packageName : packageNames) {
            eval(packageName + " = JavaImporter(Packages." + packageName + ")");
        }
        eval("joml = JavaImporter(Packages.org.joml)");
        
        eval("visual = JavaImporter(Packages.graphics.visual)");
        eval("particle = JavaImporter(Packages.graphics.particle)");
        eval("ui = JavaImporter(Packages.graphics.ui)");
        eval("collada = JavaImporter(Packages.resource.collada)");
        
        eval("GLFW = JavaImporter(org.lwjgl.glfw).GLFW;");
        eval("KeyCallbackExtender = Java.extend(Java.type(\"io.KeyCallback\"));");
        
        setCurrentObject(null);
        
        // evaluate JavaScript code from startup scripts
        String[] startupPaths = {"Script.js"};
        startupScripts = new GameScript[startupPaths.length];
        for (int i = 0; i < startupScripts.length; i++) {
            startupScripts[i] = loadScript("engine_scripts/" + startupPaths[i]);
            startupScripts[i].enable(true);
        }
    }
    
    public void release() {
        /*
        while(false) {
            
        }*/
    }
    
    public void addGLobal(String name, Object value) {
        engine.put(name, value);
    }

    private Object eval(String command) {
        try {
            return engine.eval(command);
        } catch (Exception e) {
            System.err.println(command);
            e.printStackTrace();
        }
        return null;
    }

    public void remove(GameScript obj) {
        scripts.remove(obj);
    }

    //set the engine-wide current object variable for use by child script
    private void setCurrentObject(Component parent) {
        engine.put("currentObject", parent);
    }
    private void setCurrentScript(GameScript gs) {
        engine.put("currentScript", gs);
    }
    
    public GameScript createScript(String script) {
        return createScript(null, script);
    }
    
    public GameScript createScript(Component parent, String script) {
        setCurrentObject(parent);
        GameScript gs = new GameScript(parent, script);
        setCurrentScript(gs);
        Object obj = eval(addScriptWrapper(script));
        gs.setScriptObject(obj);
        updateManager.add(gs);
        return gs;
    }
    
    public GameScript loadScript(String path) {
        return loadScript(null, path);
    }
    
    public GameScript loadScript(Component parent, String path) {
        return createScript(parent, TextData.loadText(path, resourceManager));
    }

    public Object runScriptObjectMethod(GameScript s, String func, Object... args) throws ScriptException, NoSuchMethodException {
            return inv.invokeMethod(s.getScriptObject(), func, args);
    }

    /* add basic functionality to each script
     * Each script is made into a javascript module pattern
     * Variable obj is set to the script's parent object
     * Add dispatch capability for script
     * Add special dispatch for update
     */
    private String addScriptWrapper(String textString) {
        String header = "(function() { var obj = currentObject; var script = currentScript; "
                + "function addDispatch(name, dispatch) {script.addDispatch(name, new update.Action(dispatch))};"
                + "function addUpdate(updateF) {script.addUpdate(new update.Updateable(updateF))};";
        String footer = "})()";
        return header + textString + footer;
    }
    
    
}
