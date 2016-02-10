package graphics;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

/**
 *
 * @author Andrew_2
 * 
 * An abstraction of VertexAttributes in opengl
 * One AttributeData represents a single vbo (GLBufferStruct)
 * This can represent a list of groupings of interleaved attributes
 */
public class AttributeData extends GLBufferStruct {

    private ShaderProgram owner;
    //whether or not the last grouping bound uses a ByteBuffer
    private boolean bufferLast;
    private List<List<Attribute>> attributes;

    public AttributeData(ShaderProgram sp, int target, int usage) {
        super(target, usage);
        this.owner = sp;
        attributes = new ArrayList<>();
    }

    //create a grouping without buffer backing with "size" entries
    @Override
    public void createGrouping(int size) {
        super.createGrouping(size);
        attributes.add(new ArrayList<>());
        bufferLast = false;
    }

    //create a grouping with a buffer backing
    @Override
    public void createGrouping(ByteBuffer buffer) {
        super.createGrouping(buffer);
        attributes.add(new ArrayList<>());
        bufferLast = true;
    }

    public void createAttribute(String name, GLType type) {
        createAttribute(name, type, 0);
    }
    
    //create attribute referencing buffer backing, must be specified in order
    //divisor is the AttributeDivisor
    public void createAttribute(String name, GLType type, int divisor) {
        if (!bufferLast) {
            System.err.println("cannot create a Buffer attribute without a ByteBuffer being set with createGrouping()");
            return;
        }
        Attribute a = new Attribute(name, type, divisor);
        addAttr(a);
    }
    
    public float[] createFloatAttribute(String name, GLType type) {
        return createFloatAttribute(name, type, 0);
    }
    
    //create attribute using float data
    //returns the float[] backing of the buffer
    //divisor is the AttributeDivisor
    public float[] createFloatAttribute(String name, GLType type, int divisor) {
        if(bufferLast) {
            System.err.println("cannot create a float attribute with ByteBuffer set with createGrouping()");
            return null;
        }
        Attribute a = new Attribute(name, type, divisor);
        addAttr(a);
        float[] data = createFloatData(type.components());
        return data;
    }
    
    public int[] createIntAttribute(String name, GLType type) {
        return createIntAttribute(name, type, 0);
    }
    
    //create attribute using int data
    //returns the int[] backing of the buffer
    //divisor is the AttributeDivisor
    public int[] createIntAttribute(String name, GLType type, int divisor) {
        if(bufferLast) {
            System.err.println("cannot create an int attribute with ByteBuffer set with createGrouping()");
            return null;
        }
        Attribute a = new Attribute(name, type, divisor);
        addAttr(a);
        int[] data = createIntData(type.components());
        return data;
    }
    
    private void addAttr(Attribute a) {
        attributes.get(attributes.size() - 1).add(a);
    }

    //bind the attributes to the VAO
    //must be called before use, and must be called in opengl context
    public void initialize() {
        super.compile();
        bind();
        
        for(List<Attribute> list : attributes) {
            int offset = 0;
            int stride = 0;
            for(Attribute a : list) {
                a.offset = offset;
                offset += a.type.sizeBytes();
            }
            stride = offset;
            for(Attribute a : list) {
                a.stride = stride;
            }
        }
        for(List<Attribute> list : attributes) {
            for(Attribute a : list) {
                int location = glGetAttribLocation(owner.getProgram(), a.name);
                a.location = location;
                glEnableVertexAttribArray(location);
                glVertexAttribPointer(location, a.type.components(), a.type.glType(), false, a.stride, a.offset);
                glVertexAttribDivisor(location, a.divisor);
            }
        }
    }

    private static class Attribute {

        String name;
        int location;

        GLType type;
        int offset;
        int stride;
        int divisor;

        public Attribute(String name, GLType type, int divisor) {
            this.name = name;
            this.type = type;
            this.divisor = divisor;

        }
    }
}
