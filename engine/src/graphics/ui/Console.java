package graphics.ui;

import game.GameObject;
import game.GameObjectManager;
import io.GLFWManager;
import io.KeyCallback;
import io.TextCallback;
import io.TextInput;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import org.lwjgl.glfw.GLFW;
import script.GameScript;
import script.ScriptManager;

/**
 *
 * @author Andrew_2
 */
public class Console extends KeyCallback {

    private TextDisplay display;
    private TextInput textInput;
    private GameScript consoleObject;
    private StringBuilder currentLine;
    private List<String> previousLines;
    private List<String> inputLines;
    private ScriptManager scriptManager;
    private int charWidth, charHeight, charCapacity;
    private int lineSelect;

    private StringBuilder builder;

    private static final int dispLineCacheSize = 100;
    private static final int inputLineCacheSize = 10;

    public Console(GameObject parent, TextInput textInput, int charWidth, int charHeight, int fontSize) {
        super(parent);
        this.charWidth = charWidth;
        this.charHeight = charHeight;
        charCapacity = charWidth * charHeight;
        this.display = TextDisplay.createTextDisplay("fonts/cour.ttf", 24, GLFWManager.getInstance().getResX(), GLFWManager.getInstance().getResY(), 20, 25, charCapacity);
        this.textInput = textInput;
        this.scriptManager = ScriptManager.getInstance();
        consoleObject = scriptManager.createScript(parent, "return { evaluateLine:function(line) { return eval(line); }}");

        currentLine = new StringBuilder();
        previousLines = new ArrayList<>();
        inputLines = new ArrayList<>();
        builder = new StringBuilder();
        lineSelect = 0;
    }

    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW.GLFW_RELEASE) {
            return;
        }
        processCursor(key);

        char c = textInput.parseChar(window, key, scancode, action, mods);
        if (c == '\n') {
            evalCurrentLine();
        } else if (c == '\b') {
            if (currentLine.length() > 0) {
                currentLine.setLength(currentLine.length() - 1);
            }
        } else if (c != '\u0000') {
            currentLine.append(c);
        }
        updateDisplay();
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

        display.setText(builder.toString());
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
            System.out.println("Script error");
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
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

    public static Console createConsole() {
        return createConsole("fonts/cour.ttf", 24, 40, 12);

    }

    public static Console createConsole(String fontName, int fontSize, int charWidth, int charHeight) {
        GameObject parent = GameObjectManager.getInstance().createObject();
        TextInput ti = GLFWManager.getInstance().getDefaultTextInput();
        Console c = new Console(parent, ti, charWidth, charHeight, fontSize);
        GLFWManager.getInstance().addKeyCallback(c);
        return c;
    }
}
