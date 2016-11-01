package graphics;

import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL15;
import static org.lwjgl.opengl.GL15.glGenBuffers;

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

    private boolean changed, created;
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

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    protected void create() {
        handle = glGenBuffers();
        setChanged();
        created = true;
    }

    public void release() {
        this.toRelease = true;
    }

    public void setChanged() {
        this.changed = true;
    }

    public void updateBuffer() {
        if (!isCreated()) {
            create();
        }
        if (isToRelease()) {
            destroy();
        } else if (isChanged()) {
            uploadBuffer();
        }
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

    public boolean isCreated() {
        return created;
    }

    public ByteBuffer getData() {
        return data;
    }

    public void uploadBuffer() {
        bind();
        GL15.glBufferData(target, data.capacity(), null, usage);
        GL15.glBufferSubData(target, 0, data.capacity(), data);
        changed = false;
    }

    public int getSize() {
        return data.remaining();
    }

}
