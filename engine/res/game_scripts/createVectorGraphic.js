
function createVectorGraphic(layer) {
    var cap = 100;
    var vg = visual.VectorGraphic.createAndAddVectorGraphic(cap, false, gameInst, layer);

    for (var i = 0; i < cap; i++) {
        var v1 = new joml.Vector3f(Math.cos(i), Math.sin(i), 0);
        var v2 = new joml.Vector3f(0, 0, 0);
        vg.addVector(v1, v2);
    }

    return vg;
}


return {
    
    createVectorGraphic : createVectorGraphic
};