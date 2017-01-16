function create(capacity, layer) {

    var boxGraphic = visual.BoxGraphic.createAndAddBoxGraphic(capacity, gameInst, layer);
    
    var collisionSpace = new geometry.StandardCollisionSpace(new geometry.Standard3DCollisionFilter());
    var printResponse = new geometry.PrintResponse();

    var player = createEntity(0,0,0);

    function createEntity(posX, posY, posZ) {
        var trans = new geometry.Transform(new joml.Vector3f(posX, posY, posZ));
        var bg = boxGraphic.addBox(trans);
        
        var col = new geometry.AABB3D(trans);
        col.setCollisionResponse(printResponse);
        collisionSpace.add(col);
        
        
        return {
            boxGraphic : bg,
            col : col,
            transform : trans
        }
    }

    var obstacles = new Array(capacity - 1);
    var numObstacles = 0;

    return {
        createObstacle: function (posX, posY, posZ) {
            var o = null;
            if (numObstacles < capacity - 1) {
                var o = createEntity(posX,posY,posZ);
                obstacles.push(o);
                numObstacles++;
            } else {
                LOG.error("Exceeded capacity");
            }
            return o;
        },
        player : player,
        collisionSpace : collisionSpace,

    };
}


return {

    create : create,

};



