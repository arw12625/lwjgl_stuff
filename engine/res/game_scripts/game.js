
print("Game.js Start");

with (core) {
    var collada = JavaImporter(Packages.resource.collada);
    var ui = JavaImporter(Packages.graphics.ui);
    //collada.ColladaModel.convertAndExport("misc_models/tree.dae", "misc_models/tree.json");
    //resource.WavefrontModel.convertAndExportModel("monkey", "misc_models/monkey_uv.obj", "misc_models/monkey_uv.json");
    //resource.WavefrontModel.convertAndExportModel("tree", "misc_models/tree.obj", "misc_models/tree.json");

    var tex = TextureData.loadTextureResource("misc_models/tex.png").getPath();
    h = new ui.FlatTexture();
    renderManager.add(h);
    //h.addTexture(tex, 0, 0, 1, 1, 0, 0, 0.1, 0.1);
    
    var textDisp = ui.TextDisplay.createTextDisplay(64);
    addUpdate(function (delta) {
        textDisp.setText("FPS: " + delta);
    });
    
    
    var dir = new joml.Vector3f(1, 0, 0);
    var ambient = new joml.Vector3f(.1, .1, 0.1);
    var diffuse = new joml.Vector3f(.4, 0.4, 0.4);
    var specular = new joml.Vector3f(0.1, 0.1, 0.1);
    //renderManager.addLight(new graphics.DirLight(dir, ambient, diffuse, specular));

    var dir1 = new joml.Vector3f(1, 0, 0);
    var ambient1 = new joml.Vector3f(0.1, .1, .1);
    var diffuse1 = new joml.Vector3f(0.8, .8, .8);
    var specular1 = new joml.Vector3f(0.1, .1, .1);
    renderManager.addLight(new graphics.DirLight(dir1, ambient1, diffuse1, specular1));

    var counter = 0;
    addUpdate(function (delta) {
        counter++;
        dir.x = -Math.cos(counter / 50);
        dir.z = -Math.sin(counter / 50);
        dir1.x = Math.cos(counter / 500);
        dir1.z = Math.sin(counter / 500);
    });
    
    var monkeys = [];
    var key = new io.KeyCallback(null, {
        invoke: function (window, key, scancode, action, mods) {
            if (key == GLFW.GLFW_KEY_SPACE && action == GLFW.GLFW_PRESS) {
                var obj = createSimpleRenderer("misc_models/tree.json");
                scriptManager.loadScript(obj, "game_scripts/translate.js");
                monkeys.push(obj);
            }
            if (key == GLFW.GLFW_KEY_T && action == GLFW.GLFW_PRESS) {
                var obj = createSimpleRenderer("misc_models/sphere.json");
                scriptManager.loadScript(obj, "game_scripts/translate.js");
                monkeys.push(obj);
            }

            if (key == GLFW.GLFW_KEY_R && action == GLFW.GLFW_PRESS) {
                destroy(monkeys.pop());
            }
        }
    });

}

print("Game.js End");

function createSimpleRenderer(path) {
    var obj = objManager.createObject();
    var t = new geometry.Transform(obj);
    var r = graphics.JSONRenderer.createJSONRenderer(obj, path);
    scriptManager.loadScript(obj, "game_scripts/simple_render.js");
    return obj;
}