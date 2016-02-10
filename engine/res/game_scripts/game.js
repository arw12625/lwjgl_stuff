
print("Game.js Start");

var lighting;
var trees;
var spheres;
var modelAddKey;

convertModels();

createLighting();

createHud();

createModels();

for(var i = 0; i < 10; i++) {
   visual.ParticleEmitter.createEmit(obj, "part " + i, 50, new joml.Vector3f(10 * (Math.random()-.5), 10 * (Math.random()-.5), 10 * (Math.random()-.5)));
}

print("Game.js End");


function convertModels() {

    //collada.ColladaModel.convertAndExport("misc_models/ter.dae", "misc_models/ter.json");
    //collada.ColladaModel.convertAndExport("misc_models/tree.dae", "misc_models/tree.json");
    //resource.WavefrontModel.convertAndExportModel("monkey", "misc_models/monkey_uv.obj", "misc_models/monkey_uv.json");
    //resource.WavefrontModel.convertAndExportModel("tree", "misc_models/tree.obj", "misc_models/tree.json");
}

function createLighting() {
    lighting = new visual.Lighting(gameInst, "lightBlock");
    renderManager.add(lighting);

    var dir = new joml.Vector3f(1, 0, 0);
    var ambient = new joml.Vector3f(.1, .1, 0.1);
    var diffuse = new joml.Vector3f(.4, 0.4, 0.4);
    var specular = new joml.Vector3f(0.1, 0.1, 0.1);
    //lighting.addDirLight(dir, ambient, diffuse, specular);

    var dir1 = new joml.Vector3f(1, 0, 0);
    var ambient1 = new joml.Vector3f(0.1, .1, .1);
    var diffuse1 = new joml.Vector3f(0.8, .8, .8);
    var specular1 = new joml.Vector3f(0.1, .1, .1);
    lighting.addDirLight(dir1, ambient1, diffuse1, specular1);

    var counter = 0;
    addUpdate(function (delta) {
        counter++;
        dir.x = -Math.cos(counter / 50);
        dir.z = -Math.sin(counter / 50);
        dir1.x = Math.cos(counter / 500);
        dir1.z = Math.sin(counter / 500);
    });

}

function createHud() {

    /*var tex = resource.TextureData.loadTextureResource("misc_models/tex.png").getPath();
    h = new ui.FlatTexture();
    renderManager.add(h);
    //h.addTexture(tex, 0, 0, 1, 1, 0, 0, 0.1, 0.1);*/

    

}

function createModels() {
    with (core) {

        trees = new GameObject(obj);
        spheres = new GameObject(obj);
        var models = [];
        modelAddKey = new io.KeyCallback(obj, {
            invoke: function (window, key, scancode, action, mods) {
                if (key == GLFW.GLFW_KEY_T && action == GLFW.GLFW_PRESS) {
                    var rend = createSimpleRenderer(trees, "misc_models/tree.json");
                    scriptManager.loadScript(rend, "game_scripts/translate.js");
                    models.push(rend);
                }
                if (key == GLFW.GLFW_KEY_SPACE && action == GLFW.GLFW_PRESS) {
                    var rend = createSimpleRenderer(spheres, "misc_models/sphere.json");
                    scriptManager.loadScript(rend, "game_scripts/translate.js");
                    models.push(rend);
                }

                if (key == GLFW.GLFW_KEY_R && action == GLFW.GLFW_PRESS) {
                    if (models.length > 0) {
                        models.pop().destroy();
                    }
                }
            }
        });
        glfwManager.addKeyCallback(modelAddKey);

    }
}

function createSimpleRenderer(parent, path) {
    var rend = new game.GameObject(parent);
    var r = visual.JSONRenderer.createJSONRenderer(rend, path);
    return rend;
}

return {
    getTrees : function() {return trees; }, 
    getSpheres : function() {return spheres;},
    getLighting : function() {return lighting;},
    setAddObjects : function(enable) {modelAddKey.enable(enable);}
};