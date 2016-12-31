package script;

import game.Component;
import game.Game;
import java.util.ArrayList;
import java.util.List;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger LOG = LoggerFactory.getLogger(ScriptManager.class);
    
    public static final String NO_SCRIPT_NAME = "noScriptName";

    public ScriptManager(UpdateManager updateManager, ResourceManager resourceManager) {
        LOG.info("ScriptManager constructor entered");
        scripts = new ArrayList<>();
        this.updateManager = updateManager;
        this.resourceManager = resourceManager;
        LOG.info("ScriptManager constructor exited");
    }

    public void initialize() {
        LOG.info("ScriptManager init entered");
        
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
        eval("slf4j = JavaImporter(org.slf4j);");
        eval("KeyCallbackExtender = Java.extend(Java.type(\"io.KeyCallback\"));");
        
        setCurrentObject(null);
        
        // evaluate JavaScript code from startup scripts
        String[] startupPaths = {"Script.js"};
        startupScripts = new GameScript[startupPaths.length];
        for (int i = 0; i < startupScripts.length; i++) {
            startupScripts[i] = loadScript("engine_scripts/" + startupPaths[i]);
            startupScripts[i].enable(true);
        }
        LOG.info("ScriptManager init exited");
    }
    
    public void release() {
        LOG.info("ScriptManager release entered");
        
        //for now, no cleanup is necessary
                
        LOG.info("ScriptManager release exited");
    }
    
    public void addGLobal(String name, Object value) {
        engine.put(name, value);
    }

    private Object eval(String command) {
        try {
            return engine.eval(command);
        } catch (Exception e) {
            LOG.error("{}\n {}",e,command);
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
        return createScript(parent, NO_SCRIPT_NAME, script);
    }
    
    public GameScript createScript(Component parent,String name, String script) {
        setCurrentObject(parent);
        GameScript gs = new GameScript(parent, name, script);
        setCurrentScript(gs);
        Object obj = eval(addScriptWrapper(gs));
        gs.setScriptObject(obj);
        updateManager.add(gs);
        return gs;
    }
    
    public GameScript loadScript(String path) {
        return loadScript(null, path);
    }
    
    public GameScript loadScript(Component parent, String path) {
        return loadScript(parent, NO_SCRIPT_NAME, path);
    }
    
    public GameScript loadScript(Component parent, String name, String path) {
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
    private String addScriptWrapper(GameScript gs) {
        String header = "(function() { var obj = currentObject; var script = currentScript;"
                + "var LOG = slf4j.LoggerFactory.getLogger(\"" + GameScript.class.getName() + "." + gs.getName() + "\");"
                + "function addDispatch(name, dispatch) {script.addDispatch(name, new update.Action(dispatch))};"
                + "function addUpdate(updateF) {script.addUpdate(new update.Updateable(updateF))};";
        String footer = "})()";
        return header + gs.getScript() + footer;
    }
    
    
}
