package resource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andrew_2
 * 
 * A data wrapper for binary data written into a ByteBuffer
 */
public class BufferData implements Data {

    private ByteBuffer data;
    
    private static final Logger LOG = LoggerFactory.getLogger(BufferData.class);
    
    public BufferData(){}
    
    public BufferData(ByteBuffer data) {
        this.data = data;
    }
    
    @Override
    public void load(String path, ResourceManager resourceManager) {
        
        try {
                FileInputStream fis = resourceManager.getFileInputStream(path);
                FileChannel fc = fis.getChannel();

                data = BufferUtils.createByteBuffer((int) fc.size() + 1);

                fc.read(data);

                data.flip();
                fis.close();
                fc.close();
        } catch (IOException e) {
            LOG.error("{}", e);
        }
    }
    
    @Override
    public void write(String path, ResourceManager resourceManager) {
        try {
                FileOutputStream fos = resourceManager.getFileOutputStream(path);
                FileChannel fc = fos.getChannel();

                data = BufferUtils.createByteBuffer((int) fc.size() + 1);

                fc.write(data);
                
                data.rewind();

                fos.close();
                fc.close();
        } catch (IOException e) {
            LOG.error("{}", e);
        }
    }
    
    @Override
    public boolean isValid() {
        return data != null;
    }
    
    public ByteBuffer getData() {
        return data;
    }
    
    
}
