function createCollisionTest(lighting, layer) {

    var collisionScript = scriptManager.loadScript("game_scripts/createSpriteCollisionSpace.js").getScriptObject();
    var collision = collisionScript.create(10, lighting, layer);
    var collisionSpace = collision.collisionSpace;
    var player = collision.player;

    var obst = collision.createObstacle(1,1);
    

    var playerX = 0;
    var playerY = 0;
    var playerDX = 0;
    var playerDY = 0;
    var speed = 0.1;
    var keys = new KeyCallbackExtender({
        invokeKey: function (window, key, scancode, action, mods) {
            var mul = 0;
            if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
                if (key == GLFW.GLFW_KEY_I) {
                    playerDY = 1;
                }
                if (key == GLFW.GLFW_KEY_K) {
                    playerDY = -1;
                }
                if (key == GLFW.GLFW_KEY_J) {
                    playerDX = 1;
                }
                if (key == GLFW.GLFW_KEY_L) {
                    playerDX = -1;
                }
            }
            if (action == GLFW.GLFW_RELEASE) {
                if (key == GLFW.GLFW_KEY_I) {
                    playerDY = 0;
                }
                if (key == GLFW.GLFW_KEY_K) {
                    playerDY = 0;
                }
                if (key == GLFW.GLFW_KEY_J) {
                    playerDX = 0;
                }
                if (key == GLFW.GLFW_KEY_L) {
                    playerDX = 0;
                }
            }
        }
    });
    window.addKeyCallback(keys);
    addUpdate(function (delta, layer) {
        playerX += speed * playerDX;
        playerY += speed * playerDY;
        player.setPosition(playerX, playerY);
        
        collisionSpace.detectCollisions();
        
    });


}

return {
    
    createCollisionTest : createCollisionTest,
    
};