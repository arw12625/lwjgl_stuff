theta = 0;

function update(delta) {
    theta += delta * .01;
    if(theta > 2 * 3.14159) {
        theta = 0;
    }        
    Game.renderManager.getInstance().getViewPoint().setPosition(theta - theta * theta * theta / 6 + theta * theta *theta * theta *theta /120, 0, 6);
}