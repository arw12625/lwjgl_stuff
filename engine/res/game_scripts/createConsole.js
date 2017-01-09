function createConsole(layer) {

    console = ui.Console.createConsole(gameInst, layer);

    return console;
}

return {
    createConsole : createConsole
};
