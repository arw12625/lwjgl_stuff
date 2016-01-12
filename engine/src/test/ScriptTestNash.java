package test;


import javax.script.*;

/**
 *
 * @author Andrew_2
 * 
 * testing nashorn capabilities
 */
public class ScriptTestNash {

    public static void main(String[] args) throws Exception {

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        Invocable inv = (Invocable)engine;
        
        engine.eval("a = new Packages.update.Action(function(arg) {print(arg[0]);});");
        engine.eval("a.act(7);");
        

    }
}
