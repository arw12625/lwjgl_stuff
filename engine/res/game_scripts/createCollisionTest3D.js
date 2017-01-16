

function createCollisionTest(layer) {

    var collisionScript = scriptManager.loadScript("game_scripts/createBoxCollisionSpace.js").getScriptObject();
    var collision = collisionScript.create(10, layer);
    var collisionSpace = collision.collisionSpace;
    var player = collision.player;
    player.transform.setScale(new joml.Vector3f(1,2,.5));

    var obst = collision.createObstacle(0,0,0);
    

    var playerPos = new joml.Vector3f();
    var playerVel = new joml.Vector3f();
    var speed = 0.1;
    var keys = new KeyCallbackExtender({
        invokeKey: function (window, key, scancode, action, mods) {
            var mul = 0;
            if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
                if (key == GLFW.GLFW_KEY_I) {
                    playerVel.y = 1;
                }
                if (key == GLFW.GLFW_KEY_K) {
                    playerVel.y = -1;
                }
                if (key == GLFW.GLFW_KEY_J) {
                    playerVel.x = 1;
                }
                if (key == GLFW.GLFW_KEY_L) {
                    playerVel.x = -1;
                }
                if (key == GLFW.GLFW_KEY_U) {
                    playerVel.z = 1;
                }
                if (key == GLFW.GLFW_KEY_O) {
                    playerVel.z = -1;
                }
            }
            if (action == GLFW.GLFW_RELEASE) {
                if (key == GLFW.GLFW_KEY_I) {
                    playerVel.y = 0;
                }
                if (key == GLFW.GLFW_KEY_K) {
                    playerVel.y = 0;
                }
                if (key == GLFW.GLFW_KEY_J) {
                    playerVel.x = 0;
                }
                if (key == GLFW.GLFW_KEY_L) {
                    playerVel.x = 0;
                }
                if (key == GLFW.GLFW_KEY_U) {
                    playerVel.z = 0;
                }
                if (key == GLFW.GLFW_KEY_O) {
                    playerVel.z = 0;
                }
            }
        }
    });
    window.addKeyCallback(keys);
    addUpdate(function (delta, layer) {
        playerPos.x += speed * playerVel.x;
        playerPos.y += speed * playerVel.y;
        playerPos.z += speed * playerVel.z;
        player.transform.setPosition(playerPos);
        
        collisionSpace.detectCollisions();
        
    });


}

return {
    
    createCollisionTest : createCollisionTest,
    
};