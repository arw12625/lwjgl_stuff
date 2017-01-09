

function createHeightMap(lightingBuf, layer) {
    var height = visual.HeightMap.createHeightMap(100, 100, 5, 5, gameInst, lightingBuf);
    layer.addRenderable(height, graphics.RenderLayer.DEFAULT_INDEX);
    return height;
}

return {
    
  createHeightMap: createHeightMap
};