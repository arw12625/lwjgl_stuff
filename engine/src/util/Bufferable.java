package util;

import java.nio.ByteBuffer;

/**
 *
 * @author Andrew_2
 */
public interface Bufferable {
    
    public int getSize();
    public void write(ByteBuffer b);
    
}
