package graphics.ui;

import game.GameObject;
import graphics.Renderable;
import graphics.ShaderProgram;
import graphics.UniformData;
import io.GLFWManager;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL15;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import org.lwjgl.opengl.GL30;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import resource.Resource;
import resource.TextureData;

/**
 *
 * @author Andy
 * 
 * FlatTexture renders a set of textures on the top of the frame, or according to a z-index
 * Multiple textures can be rendered on the same shader and vertex array object
 * 
 */
public class FlatTexture extends Renderable {

    private List<String> textures;
    private List<Boolean> enabled;
    private ConcurrentLinkedQueue<Integer> changed;

    private int capacity;
    private int arrayHandle;
    private int bufferHandle;
    private ShaderProgram sp;
    private UniformData ud;
    private int numTex;
    private ByteBuffer buffer;

    static final int MAX_NUMBER = 100;
    static final int NUM_BYTES = (2 + 2) * 4 * 4;

    public FlatTexture() {
        this(null, MAX_NUMBER);
    }

    public FlatTexture(GameObject parent, int capacity) {
        super(parent);
        this.capacity = capacity;
        textures = new ArrayList<>();
        changed = new ConcurrentLinkedQueue<>();
        enabled = new ArrayList<>();
        buffer = BufferUtils.createByteBuffer(capacity * NUM_BYTES);
        sp = ShaderProgram.loadProgram("shaders/flatTexture.vs", "shaders/texture.fs");
        ud = new UniformData(sp);
        sp.setUniformData(ud);
        numTex = 0;
    }

    public void addTexture(String textureName) {
        addTexture(textureName, -1, 1, 2, 2);
    }

    public void addTexture(Resource<TextureData> tex, int x, int y) {
        addTexture(tex.getPath(), tex.getData(), x, y);
    }

    //the coordinates x,y are given in pixels and the width and height of the image are used
    public void addTexture(String name, TextureData texture, int x, int y) {
        addTexture(name, x / GLFWManager.getInstance().getResX() * 2 - 1,
                y / GLFWManager.getInstance().getResY() * -2 + 1,
                texture.getImageWidth() / GLFWManager.getInstance().getResX(),
                texture.getImageHeight() / GLFWManager.getInstance().getResY());
    }

    public void addTexture(String texture, float x, float y, float width, float height) {
        addTexture(texture, x, y, width, height, 0, 0, 1, 1);
    }

    //coordinates are given as normalized opengl coordinates (-1 to 1)
    public void addTexture(String texture, float x, float y, float width, float height, float ux1, float uy1, float ux2, float uy2) {
        textures.add(texture);
        changed.add(numTex);
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

        sp.compileShader();

        arrayHandle = GL30.glGenVertexArrays();
        glBindVertexArray(arrayHandle);
        bufferHandle = glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferHandle);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, capacity * NUM_BYTES, GL_DYNAMIC_DRAW);

        int vertexLocation = glGetAttribLocation(sp.getProgram(), "vertex_position");
        glEnableVertexAttribArray(vertexLocation);
        glVertexAttribPointer(vertexLocation, 2, GL_FLOAT, false, 2 * 2 * 4, 0);

        int coordLocation = glGetAttribLocation(sp.getProgram(), "vertex_tex_coord");
        glEnableVertexAttribArray(coordLocation);
        glVertexAttribPointer(coordLocation, 2, GL_FLOAT, false, 2 * 2 * 4, 2 * 4);

    }

    @Override
    public void render() {
        Integer index;
        while((index = changed.poll()) != null) {
            buffer.position(index * NUM_BYTES);
            glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferHandle);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, NUM_BYTES * index, NUM_BYTES, buffer);
        }
        
        buffer.rewind();
        glBindVertexArray(arrayHandle);
        String lastTex = "";
        for (int i = 0; i < numTex; i++) {
            if(enabled.get(i)) {
                String texName = textures.get(i);
                if(!texName.equals(lastTex)) {
                    ud.setTexture("tex", texName);
                    ud.updateUniforms();
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
