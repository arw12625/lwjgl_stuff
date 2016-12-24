package graphics.ui;

import game.Component;
import graphics.AttributeData;
import graphics.GLType;
import graphics.RenderManager;
import graphics.Renderable;
import graphics.ShaderProgram;
import graphics.UniformData;
import graphics.VAORender;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import resource.Resource;
import resource.TextureData;

/**
 *
 * @author Andy
 *
 * FlatTexture renders a set of textures on the top of the frame, or according
 * to a z-index Multiple textures can be rendered on the same shader and vertex
 * array object
 *
 */
public class FlatTexture extends Renderable {

    private List<String> textures;
    private List<Boolean> enabled;

    private int capacity;
    private VAORender vao;
    private AttributeData attr;
    private ShaderProgram sp;
    private UniformData ud;
    private int numTex;
    private ByteBuffer buffer;

    static final int MAX_NUMBER = 100;
    static final int NUM_BYTES = (2 + 2) * 4 * 4;

    private static final String defaultVertexShader = "#version 330 core\n"
            + "\n"
            + "in vec2 vertex_position;\n"
            + "in vec2 vertex_tex_coord;\n"
            + "\n"
            + "out vec2 tex_coord;\n"
            + "out vec4 color;\n"
            + "\n"
            + "void main() {\n"
            + "    gl_Position = vec4(vertex_position, 0,1);\n"
            + "    tex_coord = vertex_tex_coord;\n"
            + "    color = vec4(1,1,1,1);\n"
            + "}";
    private static final String defaultFragmentShader = "#version 330 core\n"
            + "\n"
            + "in vec2 tex_coord;\n"
            + "in vec4 color;\n"
            + "\n"
            + "out vec4 fragColor;\n"
            + "\n"
            + "uniform sampler2D tex;\n"
            + "\n"
            + "void main()\n"
            + "{   \n"
            + "    fragColor = texture(tex, tex_coord) * color;\n"
            + "\n"
            + "}";

    public FlatTexture(ShaderProgram shaderProgram) {
        this(null, MAX_NUMBER, shaderProgram);
    }

    public FlatTexture(Component parent, int capacity, ShaderProgram shaderProgram) {
        super(parent);
        this.capacity = capacity;
        textures = new ArrayList<>();
        enabled = new ArrayList<>();
        buffer = BufferUtils.createByteBuffer(this.capacity * NUM_BYTES);

        vao = new VAORender(shaderProgram.getRenderManager());
        attr = AttributeData.createAttributeData(vao, "texture", GL_DYNAMIC_DRAW);
        attr.setData(buffer);
        attr.createAttribute("vertex_position", GLType.GL_2fv, 0, 16);
        attr.createAttribute("vertex_tex_coord", GLType.GL_2fv, 8, 16);

        this.sp = shaderProgram;
        //sp = ShaderProgram.loadProgram("shaders/flatTexture.vs", "shaders/texture.fs");
        ud = new UniformData(sp);
        sp.setUniformData(ud);
        numTex = 0;
    }

    public static FlatTexture createFlatTexture(Component parent, int capacity, RenderManager renderManager) {
        ShaderProgram defaultShader = new ShaderProgram(defaultVertexShader, defaultFragmentShader, renderManager);
        return new FlatTexture(parent, capacity, defaultShader);
    }

    public void addTexture(String textureName) {
        addTexture(textureName, -1, 1, 2, 2);
    }

    public void addTexture(Resource<TextureData> tex, int x, int y) {
        addTexture(tex.getPath(), tex.getData(), x, y);
    }

    //the coordinates x,y are given in pixels and the width and height of the image are used
    public void addTexture(String name, TextureData texture, int x, int y) {
        int windowWidth = sp.getRenderManager().getWindowWidth();
        int windowHeight = sp.getRenderManager().getWindowHeight();

        addTexture(name, x / windowWidth * 2 - 1,
                y / windowHeight * -2 + 1,
                texture.getImageWidth() / windowWidth,
                texture.getImageHeight() / windowHeight);
    }

    public void addTexture(String texture, float x, float y, float width, float height) {
        addTexture(texture, x, y, width, height, 0, 0, 1, 1);
    }

    //coordinates are given as normalized opengl coordinates (-1 to 1)
    public void addTexture(String texture, float x, float y, float width, float height, float ux1, float uy1, float ux2, float uy2) {
        textures.add(texture);
        enabled.add(Boolean.TRUE);

        buffer.position(numTex * NUM_BYTES);

        buffer.putFloat(x).putFloat(y).putFloat(ux1).putFloat(uy1);
        buffer.putFloat(x).putFloat(y - height).putFloat(ux1).putFloat(uy2);
        buffer.putFloat(x + width).putFloat(y - height).putFloat(ux2).putFloat(uy2);
        buffer.putFloat(x + width).putFloat(y).putFloat(ux2).putFloat(uy1);

        numTex++;
    }

    @Override
    public void initRender() {

        sp.createAndCompileShader();

        vao.generateVAO();
        vao.setShaderAttributeLocations(sp);
    }

    @Override
    public void render() {
        buffer.rewind();
        attr.setChanged();
        vao.useAndUpdateVAO();

        String lastTex = "";
        for (int i = 0; i < numTex; i++) {
            if (enabled.get(i)) {
                String texName = textures.get(i);
                if (!texName.equals(lastTex)) {
                    ud.setTexture("tex", texName);
                    sp.useShaderProgram();
                    lastTex = texName;
                }
                glDrawArrays(GL_QUADS, i * 4, 4);
            }
        }
    }

    @Override
    public int getZIndex() {
        return 1000;
    }

}
