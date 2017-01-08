package graphics;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.opengl.GL15;

/**
 *
 * @author Andrew_2
 */
public class VAOAttributes extends VAO {

    //map of vbo's
    Map<String,AttributeData> attributeMap;
    //optional index buffer
    GLBuffer elementArray;

    public VAOAttributes(RenderManager renderManager) {
        super(renderManager);
        attributeMap = new HashMap<>();
    }

    public void addAttributeData(AttributeData a) {
        attributeMap.put(a.getName(),a);
    }

    public void createElementArray(int usage, ByteBuffer indices) {
        elementArray = new GLBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, usage, indices);
        setElementArrayChanged();
    }

    public void setElementArrayChanged() {
        elementArray.setChanged();
    }

    //must be called in opengl context
    public void setShaderAttributeLocations(ShaderProgram sp) {
        for (AttributeData ad : attributeMap.values()) {
            ad.setShaderAttributeLocations(sp);
        }
    }

    public AttributeData getAttributeData(String name) {
        return attributeMap.get(name);
    }
    
    @Override
    protected void update() {
        
        if (elementArray != null) {
            elementArray.updateBuffer();
        }

        for (AttributeData a : attributeMap.values()) {
            a.updateBuffer();
        }
        
        super.update();
    }

}
