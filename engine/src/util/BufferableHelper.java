package util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.BufferUtils;

/**
 *
 * @author Andrew_2
 */
public class BufferableHelper<T extends Bufferable> {
    
    List<T> elements;
    ByteBuffer buffer;
    
    public BufferableHelper(ByteBuffer buffer, List<T> elements) {
        this.elements = elements;
        this.buffer = buffer;
    }
    
    public void updateBuffer() {
        buffer.rewind();
        for(Bufferable bufferable : elements) {
            bufferable.write(buffer);
        }
        buffer.rewind();
    }
    
    public ByteBuffer getByteBuffer() {
        return buffer;
    }
    
}
