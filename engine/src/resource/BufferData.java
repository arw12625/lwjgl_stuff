package resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.lwjgl.BufferUtils;

/**
 *
 * @author Andrew_2
 * 
 * A data wrapper for binary data written into a ByteBuffer
 */
public class BufferData extends Data {

    ByteBuffer data;
    
    public BufferData(){}
    
    @Override
    public void load(String path) {
        
        try {
                FileInputStream fis = ResourceManager.getInstance().getFileInputStream(path);
                FileChannel fc = fis.getChannel();

                data = BufferUtils.createByteBuffer((int) fc.size() + 1);

                while (fc.read(data) != -1);

                data.rewind();
                fis.close();
                fc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public ByteBuffer getData() {
        return data;
    }
    
    
}
