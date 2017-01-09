function createSprite3DCollection(capacity, lighting, layer) {
    var s3c = visual.Sprite3DCollection.createSprite3DCollection(capacity, gameInst, lighting);
    layer.addRenderable(s3c, graphics.RenderLayer.DEFAULT_INDEX);
    return s3c;
}

return {
    createCollection : createSprite3DCollection
    
};
