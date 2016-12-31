package graphics;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Bufferable;
import util.BufferableHelper;

/**
 *
 * @author Andrew_2
 *
 * AttributeDataStruct is an AttributeData with added functionality for writing to the buffer
 * It represents vertex attributes with an array or buffer backing.
 * A set of interleaved data is called a grouping
 * 
 * 
 */
public class AttributeDataStruct extends AttributeData {

    
    private int capacity;
    BufferableHelper bh;
    
    Map<String, Integer> groupingIndices;
    List<Bufferable> groupings;
    List<List<PreAttribute>> preAttributes;
    
    private static final Logger LOG = LoggerFactory.getLogger(AttributeDataStruct.class);
    
    public static AttributeDataStruct createAttributeDataStruct(VAORender vao, String name, int usage, RenderManager renderManager) {
        return createAttributeDataStruct(vao, name, usage, 0, renderManager);
    }
    
    public static AttributeDataStruct createAttributeDataStruct(VAORender vao, String name, int usage, int divisor, RenderManager renderManager) {
        AttributeDataStruct ad = new AttributeDataStruct(name, usage, divisor, renderManager);
        vao.addAttributeData(ad);
        return ad;
    }
    
    protected AttributeDataStruct(String name, int usage, int divisor, RenderManager renderManager) {
        super(name, usage, divisor, renderManager);
        groupingIndices = new HashMap<>();
        groupings = new ArrayList<>();
        preAttributes = new ArrayList<>();
        
        
    }

    @Override
    public void createAttribute(String name, GLType type, int offset, int stride) {
        createAttribute(name, -1, type, offset, stride);
    }
    @Override
    public void createAttribute(String name, int location, GLType type, int offset, int stride) {
        LOG.warn("Attribute created without backing data: {}", name);
        super.createAttribute(name, location, type, offset, stride);
    }
    
    public void createGrouping(String name, Bufferable grouping) {
        groupingIndices.put(name, groupings.size());
        groupings.add(grouping);
        preAttributes.add(new ArrayList<>());
    }
    
    public void createAttributeInGrouping(String groupingName, String name, GLType type) {
        createAttributeInGrouping(groupingName, name, -1, type);
    }
    public void createAttributeInGrouping(String groupingName, String name, int location, GLType type) {
        int index = groupingIndices.get(groupingName);
        preAttributes.get(index).add(new PreAttribute(name, location, type));
    }
    
    public void compile() {
        int groupingOffset = 0;
        
        for(int i = 0; i < groupings.size(); i++) {
            
            int stride = 0;
            List<Integer> offset = new ArrayList<>();
            for(int j = 0; j < preAttributes.get(i).size(); j++) {
                offset.add(stride + groupingOffset);
                stride += preAttributes.get(i).get(j).type.sizeBytes();
            }
            
            for(int j = 0; j < preAttributes.get(i).size(); j++) {
                PreAttribute pa = preAttributes.get(i).get(j);
                super.createAttribute(pa.name, pa.location, pa.type, offset.get(j), stride);
                
            }
            
            groupingOffset += groupings.get(i).getSize();
        }
        capacity = groupingOffset;
        ByteBuffer adsData = BufferUtils.createByteBuffer(capacity);
        bh = new BufferableHelper(adsData, groupings);
        setData(bh.getByteBuffer());
    }
    
    
    public int getCapacity() {
        return capacity;
    }

    //update the buffer from the backings for each grouping and update in on the GPU
    @Override
    public void updateBuffer() {
        bh.updateBuffer();
    }
    
    private static class PreAttribute {

        String name;
        int location;

        GLType type;

        public PreAttribute(String name, int location, GLType type) {
            this.name = name;
            this.location = location;
            this.type = type;

        }
    }
}
