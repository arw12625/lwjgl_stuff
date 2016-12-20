
var fps = scriptManager.loadScript(obj, "game_scripts/fps_display.js");
var movement = scriptManager.loadScript(obj, "game_scripts/viewpoint.js");
var sceneScript = scriptManager.loadScript(obj, "game_scripts/game.js").getScriptObject();
//scriptManager.loadScript(obj, "game_scripts/sound_test.js");

var conosoleScript = scriptManager.loadScript(obj, "game_scripts/create_console.js");


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
    while(!window.isCreated()){}
    
window.addKeyCallback(key);

return {
    getFPSDisp : function() {return fpsDisp;},
    getConsole : function() {return console;},
    getSceneScript : function() {return sceneScript;},
    
    addStartupScript : function(path) {
        scriptManager.loadScript(obj, path);
    }
    
};
