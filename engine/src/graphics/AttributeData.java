package graphics;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

/**
 *
 * @author Andrew_2
 *
 * An abstraction of VertexAttributes in opengl One AttributeData represents a
 * single vbo (GLBuffer) This can represent a list of InterleavedData of
 * interleaved attributes
 */
public class AttributeData extends GLBuffer {

    private final VAORender parent;
    String name;
    int divisor;
    List<Attribute> attributes;
    Map<String, Integer> attributeIndices;

    public AttributeData(VAORender parent, String name, int usage) {
        this(parent, name, usage, 0);
    }
    
    public AttributeData(VAORender parent, String name, int usage, int divisor) {
        super(GL15.GL_ARRAY_BUFFER, usage);
        this.name = name;
        parent.addAttributeData(this);
        this.parent = parent;
        attributes = new ArrayList<>();
        attributeIndices = new HashMap<>();
        this.divisor = divisor;
    }

    //create an attribute with location to be determined later
    //this can either be manually set or assigned by automatically by the shader
    //a location of -1 denotes this
    public void createAttribute(String name, GLType type) {
        createAttribute(name, type, -1);
    }

    //create an attribute with a predetermined location
    public void createAttribute(String name, GLType type, int location) {
        Attribute a = new Attribute(name, type, location);
        attributeIndices.put(name, attributes.size());
        attributes.add(a);
    }

    //bind the attributes to the VAO
    //must be called before use, and must be called in opengl context
    @Override
    protected void create() {
        super.create();
        compile();
        
        bind();
        for (Attribute a : attributes) {

            int location = a.location;
            glEnableVertexAttribArray(location);
            glVertexAttribPointer(location, a.type.components(), a.type.glType(), false, a.stride, a.offset);
            glVertexAttribDivisor(location, divisor);
        }
    }

    public String getName() {
        return name;
    }
    
    public void compile() {
        int attrOffset = 0;
        int stride = 0;
        for (Attribute a : attributes) {
            a.offset = attrOffset;
            attrOffset += a.type.sizeBytes();
        }
        stride = attrOffset;
        for (Attribute a : attributes) {
            a.stride = stride;
        }
    }

    public void setAttributeLocation(String name, int location) {
        Attribute a = attributes.get(attributeIndices.get(name));
            if (a.location == -1) {
                a.location = location;
            }
    }
    
    public void setShaderAttributeLocations(ShaderProgram sp) {
        //if shader is not compiled, asign predetermined locations to shader
        if (!sp.isCompiled()) {
            for (Attribute a : attributes) {
                if (a.location != -1) {
                    sp.bindAttributeLocation(a.name, a.location);
                }
            }
        }

        //for locations not determined, let the shader assign a location
        for (Attribute a : attributes) {
            if (a.location == -1) {
                int location = sp.getAttributeLocation(a.name);
                a.location = location;
            }
        }
        
        
    }

    private static class Attribute {

        String name;
        int location;

        GLType type;
        int offset;
        int stride;

        public Attribute(String name, GLType type, int location) {
            this.name = name;
            this.type = type;
            this.location = location;

        }
    }
}
