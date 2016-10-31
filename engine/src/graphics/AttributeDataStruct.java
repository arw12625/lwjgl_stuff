package graphics;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.BufferUtils;

/**
 *
 * @author Andrew_2
 *
 * AttributeDataStruct is an AttributeData with added functionality for writing to the buffer
 * It represents vertex attributes with an array or buffer backing.
 * A set of interleaved data is called a grouping
 * A component of a grouping is one part of the interleaved data.
 *  (Eg if the grouping has normal and vertex data interleaved, it has a normal component and vertex component)
 *
 * 
 * As of now, this class is not functional
 */
public class AttributeDataStruct extends AttributeData {

    
    private int capacity;
    private boolean bufferLast;

    //values of internal index used for other lists
    //indexed by grouping number, entries indexed by entry number
    private final List<List<Integer>> groupingIndices;
    //the number of elements in a grouping
    //indexed by grouping number
    private final List<Integer> groupingSize;
    //the number of components in an entry
    //indexed by internal index
    private final List<Integer> components;
    //the type of the entry
    //indexed by internal index
    private final List<Integer> types;

    //indexed by internal index
    private final Map<Integer, float[]> floatData;
    private final Map<Integer, int[]> intData;
    private final Map<Integer, ByteBuffer> bufferData;

    private boolean compiled;

    private final List<Grouping> groupings;
    
    //a grouping must be able to write its data to the buffer
    private static interface Grouping {
        public int getSize();
        public int getNumComponents();
        public void write(ByteBuffer data);
    }
    
    //A BufferGrouping writes data to the GLBuffer from an another existing buffer
    private static class BufferGrouping implements Grouping {
        ByteBuffer bufferData;
        int size;
        
        public void BufferGrouping(ByteBuffer bufferData) {
            this.bufferData = bufferData;
            this.size = bufferData.capacity();
        }
        
        @Override
        public void write(ByteBuffer data) {
            data.put(bufferData);
        }
    }
    
    //An ArrayGrouping will write data to the GLBuffer from an array
    private static class ArrayGrouping implements Grouping {

        @Override
        public void write(ByteBuffer data) {
            throw new UnsupportedOperationException("Not supported yet."); 
        }
        
    }
    
    
    public AttributeDataStruct(int target, int usage) {
        super(target, usage);
        currentGrouping = -1;
        currentIndex = -1;
        groupingIndices = new ArrayList<>();
        groupingSize = new ArrayList<>();
        components = new ArrayList<>();
        types = new ArrayList<>();

        floatData = new HashMap<>();
        intData = new HashMap<>();
        bufferData = new HashMap<>();

        compiled = false;
    }

    //create grouping with "size" number of elements
    public void createGrouping(int size) {
        compiled = false;
        groupingIndices.add(new ArrayList<>());
        groupingSize.add(size);
        currentGrouping++;
        bufferLast = false;
    }

    //create an entry of float data with "components" number of elements
    //returns the float[] backing this
    public float[] createFloatData(int components) {
        if (bufferLast) {
            System.err.println("cannot create a float data with a ByteBuffer bound");
            return null;
        }
        compiled = false;
        currentIndex++;
        groupingIndices.get(currentGrouping).add(currentIndex);
        this.components.add(components);
        float[] data = new float[components * groupingSize.get(currentGrouping)];
        types.add(0);
        floatData.put(currentIndex, data);
        return data;
    }

    //create an entry of int data with "components" number of elements
    //returns the int[] backing this
    public int[] createIntData(int components) {
        if (bufferLast) {
            System.err.println("cannot create a int data with a ByteBuffer bound");
            return null;
        }
        compiled = false;
        currentIndex++;
        groupingIndices.get(currentGrouping).add(currentIndex);
        this.components.add(components);
        int[] data = new int[components * groupingSize.get(currentGrouping)];
        types.add(1);
        intData.put(currentIndex, data);
        return data;
    }

    //create a grouping with backing of the supllied buffer
    //no other entries may be added to this grouping
    public void createGrouping(ByteBuffer buffer) {
        
        compiled = false;
        createGrouping(buffer.capacity());
        bufferLast = true;
        currentIndex++;
        groupingIndices.get(currentGrouping).add(currentIndex);
        this.components.add(1);
        types.add(2);
        bufferData.put(currentIndex, buffer);
    }

    //compile the groupings into one buffer
    public void compile() {
        capacity = 0;
        for (int i = 0; i < groupingIndices.size(); i++) {
            int gSize = groupingSize.get(i);
            List<Integer> indices = groupingIndices.get(i);
            for (int j = 0; j < indices.size(); j++) {
                int index = indices.get(j);
                capacity += gSize * components.get(index) * getBytesOfType(types.get(index));
            }

        }
        setData(BufferUtils.createByteBuffer(capacity));
        setChanged();
        compiled = true;
    }

    public int getCapacity() {
        return capacity;
    }

    //update the buffer from the backings for each grouping and update in on the GPU
    @Override
    public void updateBuffer() {
        if (compiled) {

            if(isChanged()) {
            ByteBuffer data = getData();
            
            data.rewind();

            for (int group = 0; group < groupingIndices.size(); group++) {
                int gSize = groupingSize.get(group);
                List<Integer> indices = groupingIndices.get(group);
                if (types.get(indices.get(0)) == 2) {
                    int index = indices.get(0);
                    data.put(bufferData.get(index));
                } else {
                    for (int i = 0; i < gSize; i++) {
                        for (int j = 0; j < indices.size(); j++) {
                            int index = indices.get(j);
                            int components = this.components.get(index);
                            switch (types.get(index)) {
                                case 0:
                                    float[] fdata = floatData.get(index);
                                    for (int k = 0; k < components; k++) {
                                        data.putFloat(fdata[i * components + k]);
                                    }
                                    break;
                                case 1:
                                    int[] idata = intData.get(index);
                                    for (int k = 0; k < components; k++) {
                                        data.putFloat(idata[i * components + k]);
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
            data.rewind();
            }
        }

        super.updateBuffer();
    }

    private int getBytesOfType(int type) {
        int bytes = 0;
        switch (type) {
            case 0:
            case 1:
                bytes = 4;
                break;
            case 2:
                bytes = 1;
        }
        return bytes;
    }
}
