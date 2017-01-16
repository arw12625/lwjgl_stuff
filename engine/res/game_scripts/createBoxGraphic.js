
function createBoxGraphic(layer) {
    var cap = 100;
    var vg = visual.BoxGraphic.createAndAddBoxGraphic(cap, gameInst, layer);

    for(var i = 0; i < 10; i++) {
        vg.addBox(new geometry.Transform( graphicsUtil.GraphicsUtility.getRandomVector3f().mul(i*2), graphicsUtil.GraphicsUtility.getRandomQuaternionf()));

    }
    return vg;
}


return {
    
    createBoxGraphic : createBoxGraphic
};

