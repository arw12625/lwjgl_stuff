function createFPSDisplay_(layer) {
    //FPS Display
    var fpsFont = ui.FontData.loadFont("fonts/arial.ttf", "fps", 24, 512, 512, new joml.Vector4f(1, 0, 1, 1), gameInst);
    var fpsDisp = ui.TextDisplay.createTextDisplay(fpsFont, 20.0, 430.0, 200.0, 200.0, 20, gameInst);
    addUpdate(function (delta, layer) {
        fpsDisp.setText("FPS: " + Math.floor(1000.0 / delta));
    });
    layer.addRenderable(fpsDisp, graphics.RenderLayer.POST_RENDER_INDEX);
    
    return fpsDisp;
}

return {

    createFPSDisplay : createFPSDisplay_

}
;