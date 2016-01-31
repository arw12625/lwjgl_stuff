package graphics.ui;

import game.GameObject;
import graphics.RenderManager;
import graphics.Renderable;
import graphics.ShaderProgram;
import graphics.UniformData;
import io.GLFWManager;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import org.lwjgl.opengl.GL15;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import org.lwjgl.opengl.GL30;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;
import resource.BufferData;
import resource.ResourceManager;
import resource.TextureData;

/**
 *
 * @author Andrew_2
 *
 * TextDisplay allows the rendering of text with various fonts using the STB lib
 * Displayed on top of the frame or according to a z-index Displayed in a
 * rectangular area
 *
 */
public class TextDisplay extends Renderable {

    private StringBuilder text;
    private boolean changed;
    private Font f;
    //pixel coordinates
    private float x, y;
    //the number of pixels wide the display can use
    //new line added when width is reached
    private float width, height;
    private FloatBuffer xPos, yPos;
    private STBTTAlignedQuad quad;

    private int charNum;

    private int capacity;
    private int arrayHandle;
    private int bufferHandle;
    private ShaderProgram sp;
    private UniformData ud;
    private ByteBuffer buffer;

    private float lineSpacing;
    private float characterSpacing;
            
    static final int defaultCapacity = 100;
    static final int NUM_BYTES = (2 + 2) * 4 * Float.BYTES;

    public TextDisplay(GameObject parent, Font f, float x, float y, float width, float height, int capacity) {

        super(parent);
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

        buffer = BufferUtils.createByteBuffer(capacity * NUM_BYTES);
        quad = STBTTAlignedQuad.create();
        sp = ShaderProgram.loadProgram("shaders/text.vs", "shaders/text.fs");
        ud = new UniformData(sp);
        int colorHandle = ud.createUniform("color", UniformData.GL_UNIFORM_TYPE.GL_4fv, 1);
        ud.setUniform(colorHandle, f.color);
        sp.setUniformData(ud);
        ud.setTexture("tex", f.textureName);
    }

    @Override
    public void initRender() {
        sp.compileShader();

        arrayHandle = GL30.glGenVertexArrays();
        glBindVertexArray(arrayHandle);
        bufferHandle = glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferHandle);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, capacity * NUM_BYTES, GL15.GL_STREAM_DRAW);

        int vertexLocation = glGetAttribLocation(sp.getProgram(), "vertex_position");
        glEnableVertexAttribArray(vertexLocation);
        glVertexAttribPointer(vertexLocation, 2, GL_FLOAT, false, 2 * 2 * 4, 0);

        int coordLocation = glGetAttribLocation(sp.getProgram(), "vertex_tex_coord");
        glEnableVertexAttribArray(coordLocation);
        glVertexAttribPointer(coordLocation, 2, GL_FLOAT, false, 2 * 2 * 4, 2 * 4);

    }

    @Override
    public void render() {

        glBindVertexArray(arrayHandle);
        RenderManager.getInstance().useShaderProgram(sp);

        //if the text has changed, all characters are recalculated
        if (changed) {
            String copy = text.toString();
            int length = copy.length();

            float xRes = GLFWManager.getInstance().getResX();
            float yRes = GLFWManager.getInstance().getResY();
            xPos.put(0, x);
            yPos.put(0, y);
            
            charNum = 0;
            buffer.rewind();
            int i = 0;
            boolean newLine = false;
            while (i < length) {
                if (newLine) {
                    xPos.put(0, x);
                    yPos.put(0, yPos.get(0) + f.fontSize + lineSpacing);
                    newLine = false;

                }
                char c = copy.charAt(i);
                if (c == '\n') {
                    i++;
                    newLine = true;
                    continue;
                }
                if (c < 32 || 128 <= c) {
                    System.err.println("unrecongnized character " + c);
                    i++;
                    continue;
                }

                STBTruetype.stbtt_GetBakedQuad(f.cdata, f.tex.getImageWidth(), f.tex.getImageHeight(), c - 32, xPos, yPos, quad, 1);
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

            glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferHandle);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer.capacity(), buffer);
            changed = false;
        }

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

    public static TextDisplay createTextDisplay(int fontSize) {
        return createTextDisplay("fonts/arial.ttf", fontSize, 200, 200);
    }

    public static TextDisplay createTextDisplay(String fontPath, int fontSize, float width, float height) {
        return createTextDisplay(fontPath, fontSize, width, height, 20, 20, defaultCapacity);
    }

    public static TextDisplay createTextDisplay(String fontPath, int fontSize, float width, float height, float x, float y, int capacity) {
        ByteBuffer b = ResourceManager.getInstance().loadResource(fontPath, new BufferData()).getData().getData();
        Font f = new Font(fontPath, b, fontSize);
        TextDisplay td = new TextDisplay(null, f, x, y, width, height, capacity);
        RenderManager.getInstance().add(td);
        return td;
    }

    @Override
    public int getZIndex() {
        return 1000;
    }

    //a class representing a true type font and associated bitmap and character map
    public static class Font {

        String name;
        String textureName;
        TextureData tex;
        STBTTBakedChar.Buffer cdata;
        int fontSize;
        Vector4f color;

        public static final int bitmapWidth = 512;
        public static final int bitmapHeight = 512;

        public Font(String name, ByteBuffer fontTTF, int fontSize) {
            this(name, fontTTF, fontSize, bitmapWidth, bitmapHeight);
        }

        public Font(String name, ByteBuffer fontTTF, int fontSize, int bitMapWidth, int bitMapHeight) {
            this(name, fontTTF, fontSize, bitMapWidth, bitMapHeight, new Vector4f(0, 0, 0, 1));
        }

        public Font(String name, ByteBuffer fontTTF, int fontSize, int bitMapWidth, int bitMapHeight, Vector4f color) {
            this.name = name;
            this.fontSize = fontSize;
            ByteBuffer bitmap = BufferUtils.createByteBuffer(bitMapHeight * bitMapWidth);
            cdata = STBTTBakedChar.createBuffer(96);
            STBTruetype.stbtt_BakeFontBitmap(fontTTF, fontSize, bitmap, bitMapWidth, bitMapHeight, 32, cdata);
            tex = new TextureData(bitmap, bitMapWidth, bitMapHeight, TextureData.TextureType.ALPHA);
            textureName = name + "_" + fontSize;
            this.color = color;
            RenderManager.getInstance().queueTexture(name + "_" + fontSize, tex);
        }
    }
}
