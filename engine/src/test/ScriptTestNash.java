package test;


import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 *
 * @author Andrew_2
 * 
 * testing nashorn capabilities
 */
public class ScriptTestNash {

    public static void main(String[] args) throws Exception {

        Integer a = 1;
        Integer b = 2;
        System.out.println(a.compareTo(b));
        
        
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        Invocable inv = (Invocable)engine;
        /*
        engine.eval("a = new Packages.update.Action(function(arg) {print(arg[0]);});");
        engine.eval("a.act(7);");
        
        Object obj = engine.eval("(function() {return {  x : 1, y :1}})()");
        System.out.println(obj);
        */
        
        /*engine.eval("console = (function() {return { evalFunc : function(code) { return  eval(code) } }})()");
        System.out.println(engine.eval("console.evalFunc('3 + 3')"));
        engine.eval("console.evalFunc('this.z = 3')");
        System.out.println(engine.eval("console.evalFunc('console.z')"));
        System.out.println(engine.eval("console.evalFunc('console')"));*/
        
        engine.eval("x = (function() {var c = 2; return {c : c, pr : function() {print(c)}}})(); x.pr();");
        
        
        
        

    }
}
