package util;

import java.nio.ByteBuffer;

/**
 *
 * @author Andrew_2
 */
public interface Bufferable {
    
    public int getBytes();
    public void write(ByteBuffer b);
    
}
