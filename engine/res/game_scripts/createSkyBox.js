function createSkyBox_(layer) {
    var sky = visual.SkyBox.createSkyBox(gameInst);
    layer.addRenderable(sky, graphics.RenderLayer.BACKGROUND_INDEX);
    return sky;
}


return {
    createSkyBox : createSkyBox_

};
