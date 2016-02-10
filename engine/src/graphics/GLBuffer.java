package graphics;

import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_INT;
import org.lwjgl.opengl.GL15;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import org.lwjgl.opengl.GL31;

/**
 *
 * @author Andrew_2
 * 
 * An abstraction of an opengl buffer
 */
public class GLBuffer {

    private ByteBuffer data;
    private int handle;
    private int target, usage;

    private boolean changed;
    private boolean toRelease;


    public GLBuffer(int target, int usage, ByteBuffer data) {
        this(target, usage);
        this.data = data;
    }
    
    public GLBuffer(int target, int usage) {
        this.target = target;
        this.usage = usage;
        handle = -1;
    }

    protected void create() {
        handle = glGenBuffers();
        setChanged();
    }

    public void release() {
        this.toRelease = true;
    }

    public void setChanged() {
        this.changed = true;
    }

    protected void updateBuffer() {
        bind();
        GL15.glBufferData(target, data.capacity(), null, usage);
        GL15.glBufferSubData(target, 0, data.capacity(), data);
        changed = false;
    }
    
    protected void bind() {
        GL15.glBindBuffer(target, handle);
    }

    protected void destroy() {
        GL15.glDeleteBuffers(handle);
    }

    public int getHandle() {
        return handle;
    }

    public boolean isToRelease() {
        return toRelease;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
        setChanged();
    }
    
    public ByteBuffer getData() {
        return data;
    }
    
    
}
