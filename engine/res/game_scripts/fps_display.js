
//FPS Display
var fpsDisp = ui.TextDisplay.createTextDisplay(obj, "fonts/arial.ttf", 24, 200, 200, 20, 430, 20, new joml.Vector4f(1,0,1,1));
    addUpdate(function (delta) {
     fpsDisp.setText("FPS: " + Math.floor(1000.0 / delta));
});
     

