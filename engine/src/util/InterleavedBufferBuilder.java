package util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.BufferUtils;

/**
 *
 * @author Andrew_2
 */
public class InterleavedBufferBuilder implements Bufferable {
    
    private ByteBuffer data;
    private int capacity;
    private int number;

    private final List<Integer> components;
    private final List<InterleavedType> types;
    private final List<Integer> indices;

    //indexed by internal index
    private final List<float[]> floatData;
    private final List<int[]> intData;
    
    
    public InterleavedBufferBuilder() {
        components = new ArrayList<>();
        types = new ArrayList<>();
        indices = new ArrayList<>();
        
        floatData = new ArrayList<>();
        intData = new ArrayList<>();
    }
    
    public void setNumber(int number) {
        this.number = number;
    }
    
    public float[] createFloatData(int components) {
        this.components.add(components);
        float[] data = new float[components * number];
        types.add(InterleavedType.FLOAT);
        indices.add(floatData.size());
        floatData.add(data);
        return data;
    }
    
    public int[] createIntData(int components) {
        this.components.add(components);
        int[] data = new int[components * number];
        types.add(InterleavedType.INTEGER);
        indices.add(intData.size());
        intData.add(data);
        return data;
    }
    
    public void compile() {
        capacity = 0;
        for(int i = 0; i < components.size(); i++) {
            capacity += components.get(i) * types.get(i).getBytes();
        }
        capacity *= number;
        data = BufferUtils.createByteBuffer(capacity);
        
    }
    
    public void updateBuffer() {
        for(int i = 0; i < number; i++) {
            for(int j = 0; j < components.size(); j++) {
                int comp = components.get(j);
                InterleavedType type = types.get(j);
                int index = indices.get(j);
                switch(type) {
                    case INTEGER: 
                        int[] idata = intData.get(index);
                        for(int k = i*comp; k < (i+1)*comp; k++) {
                            data.putInt(idata[k]);
                        }
                        break;
                    case FLOAT:
                        float[] fdata = floatData.get(index);
                        for(int k = i*comp; k < (i+1)*comp; k++) {
                            data.putFloat(fdata[k]);
                        }
                        break;
                }
            }
        }
        data.rewind();
    }
    
    @Override
    public void write(ByteBuffer b) {
        b.put(data);
    }
    
    public ByteBuffer getData() {
        return data;
    }
    
    private static enum InterleavedType {
        FLOAT(Float.BYTES), INTEGER(Integer.BYTES);
        
        private int bytes;
        private InterleavedType(int bytes) {
            this.bytes = bytes;
        }
        
        public int getBytes() {
            return bytes;
        }
    }
}
