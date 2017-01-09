function createJSONRenderer(lightingBuf, layer) {

    var mesh = visual.JSONRenderer.createJSONRenderer("misc_models/tree.json", gameInst, lightingBuf);
    layer.addRenderable(mesh, 0);
    return mesh;
}

return {
    createJSONRenderer : createJSONRenderer
};