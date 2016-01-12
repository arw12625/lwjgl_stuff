testing = function() {
    print("rawr");
};

quit = function() {
    gameInst.requestQuit();
};
exit = function() {
    gameInst.requestQuit();
};


waitUntilLoaded = function(res) {
    script.ScriptUtil.waitUntilLoaded(res);
}

