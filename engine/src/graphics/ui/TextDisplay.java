package graphics.ui;

import game.StandardGame;
import graphics.AttributeData;
import graphics.GLType;
import graphics.RenderLayer;
import graphics.RenderManager;
import graphics.Renderable;
import graphics.util.RenderableAdapter;
import graphics.ShaderProgram;
import graphics.UniformData;
import graphics.VAOAttributes;
import graphics.View;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTruetype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.ResourceManager;

/**
 *
 * @author Andrew_2
 *
 * TextDisplay allows the rendering of text with various fonts using the STB lib
 * Displayed on top of the frame or according to a z-index Displayed in a
 * rectangular area
 *
 */
public class TextDisplay extends RenderableAdapter {

    private StringBuilder text;
    private boolean changed;
    private FontData f;
    //pixel coordinates
    private float x, y;
    //the number of pixels wide the display can use
    //new line added when width is reached
    private float width, height;
    private FloatBuffer xPos, yPos;
    private STBTTAlignedQuad quad;

    private int charNum;

    private int capacity;
    private ShaderProgram sp;
    private UniformData ud;
    private VAOAttributes vao;
    private AttributeData attr;
    private ByteBuffer buffer;
    private RenderManager renderManager;

    private float lineSpacing;
    private float characterSpacing;

    static final int defaultCapacity = 100;
    static final int NUM_BYTES = (2 + 2) * 4 * Float.BYTES;

    private static final Logger LOG = LoggerFactory.getLogger(TextDisplay.class);

    public TextDisplay(FontData f, float x, float y, float width, float height, int capacity, ShaderProgram shaderProgram) {

        this.f = f;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.capacity = capacity;

        lineSpacing = 0;
        characterSpacing = 0;

        text = new StringBuilder();
        changed = false;
        xPos = BufferUtils.createFloatBuffer(1);
        yPos = BufferUtils.createFloatBuffer(1);

        renderManager = shaderProgram.getRenderManager();
        vao = new VAOAttributes(renderManager);
        attr = AttributeData.createAttributeData(vao, "text", GL_DYNAMIC_DRAW);
        buffer = BufferUtils.createByteBuffer(capacity * NUM_BYTES);
        attr.setData(buffer);
        attr.createAttribute("vertex_position", GLType.GL_2fv, 0, 16);
        attr.createAttribute("vertex_tex_coord", GLType.GL_2fv, 8, 16);

        quad = STBTTAlignedQuad.create();
        this.sp = shaderProgram;

        ud = new UniformData(sp);
        ud.addStruct(f);
        sp.setUniformData(ud);
    }

    @Override
    public void renderInit() {
        sp.createAndCompileShader();

        vao.generateVAO();
        vao.setShaderAttributeLocations(sp);

        setRenderInitialized();
    }

    @Override
    public void render(View view, RenderLayer layer) {

        //if the text has changed, all characters are recalculated
        if (changed) {
            String copy = text.toString();
            int length = copy.length();

            float xRes = renderManager.getWindowWidth();
            float yRes = renderManager.getWindowHeight();
            xPos.put(0, x);
            yPos.put(0, y);

            charNum = 0;
            buffer.rewind();
            int i = 0;
            boolean newLine = false;
            while (i < length) {
                if (newLine) {
                    xPos.put(0, x);
                    yPos.put(0, yPos.get(0) + f.getFontSize() + lineSpacing);
                    newLine = false;

                }
                char c = copy.charAt(i);
                if (c == '\n') {
                    i++;
                    newLine = true;
                    continue;
                }
                if (c < 32 || 128 <= c) {
                    LOG.error("Unrecongnized character " + c);
                    i++;
                    continue;
                }

                STBTruetype.stbtt_GetBakedQuad(f.getCdata(), f.getBitMapWidth(), f.getBitMapHeight(), c - 32, xPos, yPos, quad, 1);
                float currentWidth = quad.x1() - x;
                float currentHeight = quad.y1() - y;
                if (currentHeight > height) {
                    break;
                }
                if (currentWidth > width) {
                    newLine = true;
                    continue;
                }
                float newX0 = quad.x0() / xRes * 2 - 1f;
                float newX1 = quad.x1() / xRes * 2 - 1f;
                float newY0 = -quad.y0() / yRes * 2 + 1f;
                float newY1 = -quad.y1() / yRes * 2 + 1f;

                buffer.putFloat(newX0).putFloat(newY0).putFloat(quad.s0()).putFloat(quad.t0());
                buffer.putFloat(newX0).putFloat(newY1).putFloat(quad.s0()).putFloat(quad.t1());
                buffer.putFloat(newX1).putFloat(newY1).putFloat(quad.s1()).putFloat(quad.t1());
                buffer.putFloat(newX1).putFloat(newY0).putFloat(quad.s1()).putFloat(quad.t0());

                xPos.put(0, xPos.get(0) + characterSpacing);
                i++;
                charNum++;
            }

            buffer.rewind();

            attr.setChanged();
            changed = false;
        }

        renderManager.useAndUpdateVAO(vao);
        renderManager.useShaderProgram(sp);

        glDrawArrays(GL_QUADS, 0, charNum * 4);
    }

    @Override
    public String toString() {
        return getText();
    }

    public String getText() {
        return text.toString();
    }

    public void setText(String text) {
        this.text.setLength(0);
        this.text.append(text);
        changed = true;
    }

    public void pushChar(char c) {
        text.append(c);
        changed = true;
    }

    public void popChar() {
        if (text.length() != 0) {
            text.deleteCharAt(text.length() - 1);

        }
        changed = true;
    }

    public static TextDisplay createTextDisplay(FontData font,
            float x, float y, float width, float height,
            int capacity, StandardGame game) {
        return createTextDisplay(font, x, y,width, height,
                capacity, game.getRenderManager(), game.getResourceManager());
    }

    public static TextDisplay createTextDisplay(
            FontData font, float x, float y, float width, float height,
            int capacity, RenderManager renderManager, ResourceManager resourceManager) {
        ShaderProgram shader = ShaderProgram.loadProgram("shaders/text.vs", "shaders/text.fs", renderManager, resourceManager);
        TextDisplay td = new TextDisplay(font, x, y, width, height, capacity, shader);
        return td;
    }

}
