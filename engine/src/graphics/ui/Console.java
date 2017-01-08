package graphics.ui;

import game.StandardGame;
import graphics.RenderLayer;
import graphics.RenderManager;
import graphics.Renderable;
import graphics.View;
import graphics.util.RenderLayer2D;
import io.KeyCallback;
import io.TextInput;
import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptException;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.ResourceManager;
import resource.TextureData;
import script.GameScript;
import script.ScriptManager;

/**
 *
 * @author Andrew_2
 */
public class Console implements KeyCallback {

    private TextDisplay textDisplay;
    private TextInput textInput;
    private FlatTexture background;
    
    private boolean isEnabled, isPendingRelease;
    
    private RenderManager renderManager;
    private RenderLayer renderLayer;
    private ResourceManager resourceManager;
    private ScriptManager scriptManager;
    private GameScript consoleObject;
    
    private StringBuilder currentLine;
    private List<String> previousLines;
    private List<String> inputLines;
    private StringBuilder builder;
    
    private int charWidth, charHeight, charCapacity;
    private int lineSelect;
    private int fontSize;

    private static final int dispLineCacheSize = 100;
    private static final int inputLineCacheSize = 10;
    private static final String[] deleteDelimiters = {" ", "\n", "(", ")", "[", "]", "{", "}", "\"", "'", ":", "."};
    private static final String defaultFont = "fonts/cour.ttf";
    private static final Vector4f defaultColor = new Vector4f(0.5f, 0.5f, .5f, 1);
    
    private static final int TEXT_RENDER_INDEX = 100;
    private static final int BACKGROUND_RENDER_INDEX = 0;
    private static final int DEFAULT_RENDER_LAYER_INDEX = graphics.RenderLayer.UI_INDEX;


    private static final Logger LOG = LoggerFactory.getLogger(Console.class);

    public Console(TextInput textInput, int charWidth, int charHeight, int fontSize,
            RenderManager renderManager, ResourceManager resourceManager, ScriptManager scriptManager,
            RenderLayer renderLayer) {
        this.charWidth = charWidth;
        this.charHeight = charHeight;
        charCapacity = charWidth * charHeight;

        this.fontSize = fontSize;
        this.textInput = textInput;
        this.scriptManager = scriptManager;
        this.renderManager = renderManager;
        this.resourceManager = resourceManager;
        this.renderLayer = renderLayer;

        currentLine = new StringBuilder();
        previousLines = new ArrayList<>();
        inputLines = new ArrayList<>();
        builder = new StringBuilder();
        lineSelect = 0;
    }

    public void init() {
        renderManager.getWindow().addKeyCallback(this);
        
        FontData f = FontData.loadFont(defaultFont, "consoleFont", fontSize, 512, 512, defaultColor, renderManager, resourceManager);
        this.textDisplay = TextDisplay.createTextDisplay(f, 30, 40,
                renderManager.getWindowWidth(), renderManager.getWindowHeight(),
                charCapacity, renderManager, resourceManager);
        renderLayer.addRenderable(textDisplay, TEXT_RENDER_INDEX);

        TextureData.loadTextureResource("console/console.png", renderManager, resourceManager);
        background = FlatTexture.createFlatTexture(10, renderManager);
        background.addTexture("console/console.png", -1, 1, 2, 2);
        renderLayer.addRenderable(background, BACKGROUND_RENDER_INDEX);

        consoleObject = scriptManager.loadScript("game_scripts/console.js");
    }

    private void processCursor(int key) {
        boolean changed = false;
        switch (key) {
            case GLFW.GLFW_KEY_UP:
                lineSelect--;
                changed = true;
                break;
            case GLFW.GLFW_KEY_DOWN:
                lineSelect++;
                changed = true;
                break;
            default:
                break;
        };
        if (changed) {
            if (lineSelect < 0) {
                lineSelect = inputLines.size() - 1;
            } else if (lineSelect > inputLines.size() - 1) {
                lineSelect = 0;
            }
            if (!inputLines.isEmpty()) {
                currentLine.setLength(0);
                currentLine.append(inputLines.get(lineSelect));
            }
        }
    }

    private void evalCurrentLine() {

        String line = currentLine.toString();
        inputLines.add(line);
        if (inputLines.size() > inputLineCacheSize) {
            inputLines.remove(0);
        }
        addLine(line);
        currentLine.setLength(0);
        lineSelect = 0;

        eval(line);

    }

    private void updateDisplay() {
        String currentString = currentLine.toString();
        List<String> currentLines = formatLine(currentString);
        int numLines = Math.min(currentLines.size(), charHeight);
        int remaining = Math.min(previousLines.size(), charHeight - numLines);
        builder.setLength(0);
        for (int i = previousLines.size() - remaining; i < previousLines.size(); i++) {
            builder.append(previousLines.get(i));
            builder.append('\n');
        }
        for (int i = currentLines.size() - numLines; i < currentLines.size(); i++) {
            builder.append(currentLines.get(i));
            builder.append('\n');
        }

        textDisplay.setText(builder.toString());
    }

    private List<String> formatLine(String line) {
        List<String> lines = new ArrayList<>();
        if (line.isEmpty()) {
            lines.add("");
        } else {
            String[] split = line.split("\n");
            for (String s : split) {
                for (int i = 0; i < s.length(); i += charWidth) {
                    lines.add(s.substring(i, Math.min(s.length(), i + charWidth)));
                }
            }
        }
        return lines;
    }

    private void eval(String line) {
        try {
            scriptManager.runScriptObjectMethod(consoleObject, "evaluateLine", line);
        } catch (ScriptException ex) {
            println("Script error");
            LOG.error("{}", ex);
        } catch (NoSuchMethodException ex) {
            LOG.error("{}", ex);
        }
    }

    public void clearConsole() {
        previousLines.clear();
        currentLine.setLength(0);
        updateDisplay();
    }

    public void print(String line) {
        addLine(line);
        updateDisplay();
    }

    public void println(String line) {
        print(line + "\n");
    }

    private void addLine(String line) {
        previousLines.addAll(formatLine(line));
        while (previousLines.size() > dispLineCacheSize) {
            previousLines.remove(0);
        }
    }

    public static Console createConsole(StandardGame game) {
        return createConsole(24, 40, 12, game);

    }

    public static Console createConsole(int fontSize, int charWidth, int charHeight, StandardGame game) {
        
        RenderLayer2D layer = RenderLayer2D.createRenderLayer2D();
        game.getRenderManager().addRenderLayer(layer, DEFAULT_RENDER_LAYER_INDEX);
        
        return createConsole(fontSize, charWidth, charHeight, game, layer);
    }
    
    public static Console createConsole(StandardGame game, RenderLayer layer) {
        return createConsole(24, 40, 12, game, layer);
    }
    
    public static Console createConsole(int fontSize, int charWidth, int charHeight, StandardGame game, RenderLayer layer) {
        
        TextInput ti = game.getRenderManager().getWindow().getDefaultTextInput();

        
        Console c = new Console(ti, charWidth, charHeight, fontSize,
                game.getRenderManager(), game.getResourceManager(), game.getScriptManager(),
                layer);
        c.init();
        c.consoleEnable(false);

        return c;
    }
    

    public GameScript getScript() {
        return consoleObject;
    }

    @Override
    public void invokeKey(long window, int key, int scancode, int action, int mods) {

        if (action == GLFW.GLFW_RELEASE) {
            return;
        }
        processCursor(key);

        char c = textInput.parseChar(window, key, scancode, action, mods);
        if (c == '\n') {
            if (mods != GLFW.GLFW_MOD_SHIFT) {
                evalCurrentLine();
            } else {
                currentLine.append('\n');
            }
        } else if (c == '\b') {
            if (mods == GLFW.GLFW_MOD_CONTROL) {
                int newLength = -1;
                for (String s : deleteDelimiters) {
                    newLength = Math.max(newLength, currentLine.lastIndexOf(s));
                }
                newLength = Math.min(newLength + 1, currentLine.length() - 1);
                newLength = Math.max(newLength, 0);
                currentLine.setLength(newLength);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.setLength(currentLine.length() - 1);
                }
            }
        } else if (c == '\t') {
            currentLine.append("   ");
        } else if (c == GLFW.GLFW_KEY_GRAVE_ACCENT) {

        } else if (c != '\u0000') {
            currentLine.append(c);
        }
        updateDisplay();
    }
    
    public void consoleEnable(boolean enable) {
        this.isEnabled = enable;
        textDisplay.renderEnable(enable);
        background.renderEnable(enable);
        
    }
    
    public void requestConsoleRelease() {
        this.isPendingRelease = true;
        textDisplay.renderRelease();
        background.renderRelease();
    }
    
    public boolean isConsoleEnabled() {
        return isEnabled;
    }

    public boolean isConsolePendingRelease() {
        return isPendingRelease;
    }

    @Override
    public boolean isKeyCallbackEnabled() {
        return isConsoleEnabled();
    }

    @Override
    public boolean isKeyCallbackPendingRelease() {
        return isConsolePendingRelease();
    }

}
