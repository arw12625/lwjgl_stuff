
function createCamera() {
    var fixed = false;
    var x = 0;
    var y = 0;
    var z = 0;
    var speed = 0.1;
    var mouseSpeedX = -0.01;
    var mouseSpeedY = -0.01;

    var camera = new graphicsUtil.Camera();
    var aspectRatio = renderManager.getWindowWidth() / renderManager.getWindowHeight();
    camera.setPespectiveProjection(3.14 / 3, aspectRatio, .1, 100);

    var keys = new KeyCallbackExtender({
        invokeKey: function (window, key, scancode, action, mods) {
            var mul = 0;
            if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
                if (key == GLFW.GLFW_KEY_S) {
                    z = 1;
                }
                if (key == GLFW.GLFW_KEY_W) {
                    z = -1;
                }
                if (key == GLFW.GLFW_KEY_D) {
                    x = 1;
                }
                if (key == GLFW.GLFW_KEY_A) {
                    x = -1;
                }
                if (key == GLFW.GLFW_KEY_LEFT_SHIFT) {
                    y = 1;
                }
                if (key == GLFW.GLFW_KEY_LEFT_CONTROL) {
                    y = -1;
                }
            }
            if (action == GLFW.GLFW_RELEASE) {
                if (key == GLFW.GLFW_KEY_S) {
                    z = 0;
                }
                if (key == GLFW.GLFW_KEY_W) {
                    z = 0;
                }
                if (key == GLFW.GLFW_KEY_D) {
                    x = 0;
                }
                if (key == GLFW.GLFW_KEY_A) {
                    x = 0;
                }
                if (key == GLFW.GLFW_KEY_LEFT_SHIFT) {
                    y = 0;
                }
                if (key == GLFW.GLFW_KEY_LEFT_CONTROL) {
                    y = 0;
                }
            }
        }
    });
    window.addKeyCallback(keys);

    window.bindCursor();

    var mx = 0;
    var my = 0;
    addUpdate(function (delta, layer) {
        if(!fixed) {
        camera.moveLocalCoords(speed * x, speed * y, speed * z);
        var dy = window.getDMouseX() * mouseSpeedX;
        var dx = window.getDMouseY() * mouseSpeedY;
        mx += dx;
        my += dy;
        if (mx > Math.PI / 2) {
            mx = Math.PI / 2;
        }
        if (mx < -Math.PI / 2) {
            mx = -Math.PI / 2;
        }
        camera.setYXAngle(mx, my);

    }
    });

    return {
        fix : function(ena) {fixed = ena;},
        getCamera : function() {return camera;}
    }
}

return {
    createCamera : createCamera
}
;
