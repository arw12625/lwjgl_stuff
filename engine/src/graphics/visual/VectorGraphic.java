package graphics.visual;

import game.Component;
import graphics.RenderManager;
import graphics.Renderable;
import graphics.ShaderProgram;
import graphics.UniformData;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
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


/**
 *
 * @author Andy
 * 
 * VectorGraphic enables the display of a vector with world location and direction
 * Not tested
 * 
 */
public class VectorGraphic extends Renderable {

    public static final int NORMAL_Z_INDEX = RenderManager.DEFAULT_Z_INDEX;
    public static final int XRAY_Z_INDEX = 100;
    
    int numVec;
    int capacity;
    boolean xray;
    private int arrayHandle;
    private int bufferHandle;
    ShaderProgram sp;
    UniformData ud;
    Queue<Integer> changed;
    ByteBuffer buffer;
    
    int pvHandle;
    
    public static final int NUM_BYTES = 2 * 3 * Float.BYTES;
    
    public VectorGraphic(Component parent, int capacity, boolean xray) {
        super(parent);
        this.capacity = capacity;
        this.xray = xray;
        
        changed = new ConcurrentLinkedQueue<>();
        buffer = BufferUtils.createByteBuffer(capacity * NUM_BYTES);
        
        ShaderProgram sp = ShaderProgram.loadProgram("vector.vs", "vector.fs");
        ud = new UniformData(sp);
        sp.setUniformData(ud);
        numVec = 0;
                
    }
    
    
    @Override
    public void initRender() {

        sp.createAndCompileShader();

        arrayHandle = GL30.glGenVertexArrays();
        glBindVertexArray(arrayHandle);
        bufferHandle = glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferHandle);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, capacity * NUM_BYTES, GL_DYNAMIC_DRAW);

        int vertexLocation = glGetAttribLocation(sp.getProgram(), "vertex_position");
        glEnableVertexAttribArray(vertexLocation);
        glVertexAttribPointer(vertexLocation, 3, GL_FLOAT, false, 0, 0);

        //pvHandle = ud.createUniform("projection_model", UniformData.GL_UNIFORM_TYPE.GL_m4fv, 1);
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
                
        ud.setUniform(pvHandle, RenderManager.getInstance().getProjectionViewMatrix());
        for (int i = 0; i < numVec; i++) {
                glDrawArrays(GL11.GL_LINES, i * 2, 2);
        }
    }

    @Override
    public int getZIndex() {
        return 1000;
    }
    
    public void addVector(Vector3f pos, Vector3f vec) {
        addVector(pos, vec, false);
    }
    
    public void addVector(Vector3f pos, Vector3f vec, boolean normalised) {
        Vector3f dir = new Vector3f();
        if(normalised && vec.lengthSquared() != 0) {
            vec.normalize(dir);
        } else {
            dir = vec;
        }
        buffer.position(NUM_BYTES * numVec);
        numVec++;
        buffer.putFloat(pos.x).putFloat(pos.y).putFloat(pos.z);
        buffer.putFloat(dir.x).putFloat(dir.y).putFloat(dir.z);
    }
}
