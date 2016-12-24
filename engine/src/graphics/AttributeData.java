package graphics;

import java.util.HashMap;
import java.util.Map;
import org.lwjgl.opengl.GL15;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

/**
 *
 * @author Andrew_2
 *
 * An abstraction of VertexAttributes in opengl One AttributeData represents a
 * single vbo (GLBuffer)
 *
 */
public class AttributeData extends GLBuffer {

    String name;
    int divisor;
    Map<String, Attribute> attributes;

    public static AttributeData createAttributeData(VAORender vao, String name, int usage) {
        return createAttributeData(vao, name, usage, 0);
    }
    
    public static AttributeData createAttributeData(VAORender vao, String name, int usage, int divisor) {
        AttributeData ad = new AttributeData(name, usage, divisor, vao.getRenderManager());
        vao.addAttributeData(ad);
        return ad;
    }
    
    protected AttributeData(String name, int usage, int divisor, RenderManager renderManager) {
        super(GL15.GL_ARRAY_BUFFER, usage, renderManager);
        this.name = name;
        attributes = new HashMap<>();
        this.divisor = divisor;
    }

    //create an attribute with location to be determined later
    //this can either be manually set or assigned by automatically by the shader
    //a location of -1 denotes this
    public void createAttribute(String name, GLType type, int offset, int stride) {
        createAttribute(name, -1, type, offset, stride);
    }

    //create an attribute with a predetermined location
    public void createAttribute(String name, int location, GLType type, int offset, int stride) {
        Attribute a = new Attribute(name, location, type, offset, stride);
        attributes.put(name, a);
    }

    //bind the attributes to the VAO
    //must be called before use, and must be called in opengl context
    @Override
    protected void create() {
        super.create();
        //compile();

        bind();
        for (Attribute a : attributes.values()) {

            int location = a.location;
            glEnableVertexAttribArray(location);
            glVertexAttribPointer(location, a.type.components(), a.type.glType(), false, a.stride, a.offset);
            glVertexAttribDivisor(location, divisor);
        }
    }

    public String getName() {
        return name;
    }

    /*
     Used when stride and offset were predetermined
     private void compile() {
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
     }*/
    //setAttributeLocation does not override existing attribute locations
    public void setAttributeLocation(String name, int location) {
        Attribute a = attributes.get(name);
        if (a.location == -1) {
            a.location = location;
        }
    }

    public void coerceAttributeLocation(String name, int location) {
        Attribute a = attributes.get(name);
        a.location = location;
    }

    public void setShaderAttributeLocations(ShaderProgram sp) {
        //if shader is not compiled, asign predetermined locations to shader
        if (!sp.isCompiled()) {
            for (Attribute a : attributes.values()) {
                if (a.location != -1) {
                    sp.bindAttributeLocation(a.name, a.location);
                }
            }
        }

        //for locations not determined, let the shader assign a location
        for (Attribute a : attributes.values()) {
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

        public Attribute(String name, int location, GLType type, int offset, int stride) {
            this.name = name;
            this.location = location;
            this.type = type;
            this.offset = offset;
            this.stride = stride;

        }
    }
}
