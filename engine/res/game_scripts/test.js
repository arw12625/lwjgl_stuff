
renderManager.addView(new graphicsUtil.UIViewPort(0, 0, 640, 480));
var layer2D = graphicsUtil.RenderLayer2D.createRenderLayer2D();
renderManager.addRenderLayer(layer2D, graphics.RenderLayer.UI_INDEX);

var cameraScript = scriptManager.loadScript("game_scripts/createCamera.js");
var cameraObj = cameraScript.getScriptObject().createCamera();
var camera = cameraObj.getCamera();
renderManager.addView(new graphicsUtil.CameraViewPort(camera, 0, 0, 640, 480));
var layer3D = graphicsUtil.RenderLayer3D.createRenderLayer3D();
renderManager.addRenderLayer(layer3D, graphics.RenderLayer.DEFAULT_INDEX);

var consoleScript = scriptManager.loadScript("game_scripts/createConsole.js").getScriptObject();
var console = consoleScript.createConsole(layer2D);
var fpsDisplayScript = scriptManager.loadScript("game_scripts/createFPSDisplay.js").getScriptObject();
var fps = fpsDisplayScript.createFPSDisplay(layer2D);

var lightingScript = scriptManager.loadScript("game_scripts/createLighting.js").getScriptObject();
var lighting = lightingScript.createLighting();
var lightingBuf = lightingScript.createUniformBuffer(lighting, layer3D);

var heightScript = scriptManager.loadScript("game_scripts/createHeightMap.js").getScriptObject();
var height = heightScript.createHeightMap(lightingBuf, layer3D);

/*
var meshScript = scriptManager.loadScript("game_scripts/createJSONRenderer.js").getScriptObject();
var mesh = meshScript.createJSONRenderer(lightingBuf, layer3D);

var vectorScript = scriptManager.loadScript("game_scripts/createVectorGraphic.js").getScriptObject();
var vector = vectorScript.createVectorGraphic(layer3D);

var skyBoxScript = scriptManager.loadScript("game_scripts/createSkyBox.js").getScriptObject();
var sky = skyBoxScript.createSkyBox(layer3D);

var soundScript = scriptManager.loadScript("game_scripts/sound_test.js");
*/

var spriteScript = scriptManager.loadScript("game_scripts/createCollisionTest.js").getScriptObject();
var col = spriteScript.createCollisionTest(lightingBuf, layer3D);

var key = new io.KeyCallback({
    invokeKey: function (window, key, scancode, action, mods) {
        if (key == GLFW.GLFW_KEY_GRAVE_ACCENT && action == GLFW.GLFW_PRESS) {
            var ena = !console.isConsoleEnabled();
            console.consoleEnable(ena);
            cameraObj.fix(ena);
        }
    }
});


window.addKeyCallback(key);
