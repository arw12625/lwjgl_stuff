function create(capacity, lighting, layer) {

    var spriteScript = scriptManager.loadScript("game_scripts/createSprite3DCollection.js").getScriptObject();
    var spriteCollection = spriteScript.createCollection(capacity, lighting, layer);
    
    var collisionSpace = new geometry.StandardCollisionSpace2D(new geometry.StandardCollision2D());
    var printResponse = new geometry.PrintResponse();

    var blockTex = "misc_models/0.png";
    resource.TextureData.loadTextureResource(blockTex, gameInst);
    var playerTex = "misc_models/1.png";
    resource.TextureData.loadTextureResource(playerTex, gameInst);

    var player = createEntity(0,0,playerTex);
    player.enable(true);

    function createEntity(posX, posY, tex) {
        var sprite = spriteCollection.createSprite();
        sprite.useTexture(tex);
        sprite.enableTexture(true);
        sprite.setColor(new joml.Vector4f(1, 1, 1, 1));

        var col = new geometry.AABB2D();
        col.setWidth(2);
        col.setHeight(2);
        col.setCollisionResponse(printResponse);
        collisionSpace.add(col);
        
        setPosition(posX, posY);
        enable(true);

        function setPosition(xPos, yPos) {
            sprite.setRectangle(new joml.Vector3f(xPos, yPos, 0), new joml.Quaternionf());
            col.setPosition(xPos, yPos);
        }
        
        function enable(ena) {
            sprite.enable(ena);
        }

        return {
            sprite : sprite,
            col : col,
            enable : enable,
            setPosition : setPosition,
        }
    }

    var obstacles = new Array(capacity - 1);
    var numObstacles = 0;

    return {
        createObstacle: function (posX, posY) {
            var o = null;
            if (numObstacles < capacity - 1) {
                var o = createEntity(posX,posY,blockTex);
                obstacles.push(o);
                o.enable(true);
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

