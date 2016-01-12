var renderer = obj.getComponent("JSONRenderer");
var trans = obj.getComponent("Transform");

var meshes = renderer.getMeshes();
var initTrans = []
var pvmLocs = [];
var vmLocs = [];
var normalLocs = [];
for(var i = 0; i < meshes.size(); i++) {
    var ud = meshes.get(i).getUniforms();
    initTrans.push(resource.JSONData.parseMat(meshes.get(i).getJSON().getString("transform")));
    pvmLocs.push(ud.createUniform("proj_view_model", graphics.UniformData.GL_UNIFORM_TYPE.GL_m4fv, 1));
    vmLocs.push(ud.createUniform("view_model", graphics.UniformData.GL_UNIFORM_TYPE.GL_m4fv, 1));
    normalLocs.push(ud.createUniform("normal_mat", graphics.UniformData.GL_UNIFORM_TYPE.GL_m3fv, 1));
    ud.setUniformBuffer("lightBlock", "lightBlock");
}

var pvmMat = new joml.Matrix4f();
var viewMat = new joml.Matrix4f();
var normalMat = new joml.Matrix3f();
        

addUpdate(function (delta) {
    for(var i = 0; i < meshes.size(); i++) {
        
    var transMat = trans.toMatrix();
    transMat.mul(initTrans[i], transMat);
    renderManager.getProjectionViewMatrix().mul(transMat, pvmMat);

    renderManager.getViewMatrix().mul(transMat, viewMat);

    normalMat.set(viewMat);
    normalMat.invert().transpose();
    
        var ud = meshes.get(i).getUniforms();
        ud.setUniform(pvmLocs[i], pvmMat);
        ud.setUniform(vmLocs[i], viewMat);
        ud.setUniform(normalLocs[i], normalMat);
    }
    
});