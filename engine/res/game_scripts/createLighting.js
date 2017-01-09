
function createLighting() {

    var lighting = visual.Lighting.createLighting("EHHEHE", 2);
    var dir1 = new joml.Vector3f(.707, -.707, 0);
    var ambient1 = new joml.Vector3f(0.1, .1, .1);
    var diffuse1 = new joml.Vector3f(0.8, .8, .8);
    var specular1 = new joml.Vector3f(0.01, .01, .01);
    lighting.addDirLight(dir1, ambient1, diffuse1, specular1);
    return lighting;
}

return {
  
    createLighting : createLighting,
    createUniformBuffer: function(lighting, layer) {return lighting.createAndAddUniformBuffer(layer);}
    
};

