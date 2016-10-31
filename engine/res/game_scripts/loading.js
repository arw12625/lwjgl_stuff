

var fpsDisp = ui.TextDisplay.createTextDisplay(obj, "fonts/arial.ttf", 24, 200, 200, 20, 430, 20, new joml.Vector4f(1,0,1,1));
    addUpdate(function (delta) {
     fpsDisp.setText("FPS: " + Math.floor(1000.0 / delta));
});
     
    
var movement = scriptManager.loadScript(obj, "game_scripts/viewpoint.js");
var sceneScript = scriptManager.loadScript(obj, "game_scripts/game.js").getScriptObject();
//scriptManager.loadScript(obj, "game_scripts/sound_test.js");

var console = ui.Console.createConsole(obj);
console.getScript().getScriptObject().scene = obj;


var spheres = sceneScript.getSpheres();

var key = new io.KeyCallback(obj, {
        invoke: function (window, key, scancode, action, mods) {
            if (key == GLFW.GLFW_KEY_GRAVE_ACCENT && action == GLFW.GLFW_PRESS) {
                var ena = !console.isEnabled();
                console.enable(ena);
                spheres.enable(!ena);
                movement.enable(!ena);
            }
        }
    });
glfwManager.addKeyCallback(key);

return {
    getFPSDisp : function() {return fpsDisp;},
    getConsole : function() {return console;},
    getSceneScript : function() {return sceneScript;}
    
    
};
