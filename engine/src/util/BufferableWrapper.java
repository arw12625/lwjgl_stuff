package util;

import java.nio.ByteBuffer;

/**
 *
 * @author Andrew_2
 */
public class BufferableWrapper implements Bufferable {
    private ByteBuffer buffer;
    
    public BufferableWrapper(ByteBuffer buffer) {
        this.buffer = buffer.slice();
    }

    @Override
    public int getSize() {
        return buffer.capacity();
    }

    @Override
    public void write(ByteBuffer b) {
        b.put(buffer);
        buffer.rewind();
    }
    
    
}
