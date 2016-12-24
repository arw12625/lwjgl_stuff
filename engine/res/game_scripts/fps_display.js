
//FPS Display
var fpsFont = ui.FontData.loadFont("fonts/arial.ttf", "fps", 24, 512, 512, new joml.Vector4f(1,0,1,1), gameInst);
var fpsDisp = ui.TextDisplay.createTextDisplay(obj, fpsFont, 200, 200, 20, 430, 20, gameInst);
    addUpdate(function (delta) {
     fpsDisp.setText("FPS: " + Math.floor(1000.0 / delta));
});
     

