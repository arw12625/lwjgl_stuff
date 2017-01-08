package graphics.visual;

import graphics.util.Camera;
import graphics.GLBuffer;
import graphics.util.HasCamera;
import graphics.RenderLayer;
import graphics.RenderManager;
import graphics.util.UniformBufferable;
import graphics.View;
import graphics.util.GraphicsUtility;
import graphics.util.RenderLayer3D;
import graphics.util.UniformBufferableRenderer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utilities;

/**
 *
 * @author Andrew_2
 */
public class Lighting implements UniformBufferable {

    private String name;
    
    private int maxNumLights;
    private int bufferSize;
    
    private List<DirLight> dirLights;
    private int numDirLights;
    
    private static final int HEADER_SIZE = 4*Float.BYTES;
    private static final int LIGHT_SIZE = 4*4*Float.BYTES;
    
    private static final Logger LOG = LoggerFactory.getLogger(Lighting.class);
    
    
    public static final int DEFAULT_MAX_LIGHTS = 16;
    public static final int DEFAULT_LIGHTING_INDEX = RenderLayer.PRE_RENDER_INDEX;

    public Lighting(String name) {
        this(name, DEFAULT_MAX_LIGHTS);   
    }
    
    public Lighting(String name, int maxNumLights) {
        
        this.name = name;
        this.maxNumLights = maxNumLights;
        this.bufferSize = LIGHT_SIZE * maxNumLights + HEADER_SIZE;
        dirLights = new ArrayList<>();
        numDirLights = 0;

    }
    
    public static Lighting createLighting(String name, int maxNumLights) {
        return new Lighting(name, maxNumLights);
    }
    
    public UniformBufferableRenderer createAndAddUniformBuffer(RenderLayer layer) {
        return createAndAddUniformBuffer(layer, DEFAULT_LIGHTING_INDEX);
    }
    
    public UniformBufferableRenderer createAndAddUniformBuffer(RenderLayer layer, int index) {
        UniformBufferableRenderer u = new UniformBufferableRenderer(this);
        layer.addRenderable(u, index);
        return u;
    }
    
    
    @Override
    public GLBuffer createBuffer(String bufName) {
        
        ByteBuffer buffer = BufferUtils.createByteBuffer(bufferSize); 
        GLBuffer glBuffer = new GLBuffer(GL31.GL_UNIFORM_BUFFER, GL15.GL_DYNAMIC_DRAW, buffer);
        return glBuffer;
    }
    
    @Override
    public void writeBuffer(GLBuffer glBuffer, View view, RenderLayer layer) {
        Camera c = GraphicsUtility.getHackyCamera(view);
        updateBuffer(glBuffer, c);
    }
    
    public void updateBuffer(GLBuffer glBuffer, Camera c) {
        ByteBuffer buffer = glBuffer.getData();
        buffer.position(HEADER_SIZE);
        Matrix4f view = c.getViewMatrix();
        int i = 0;
        while(i < numDirLights) {
            DirLight light = dirLights.get(i);
            if(light.remove) {
                dirLights.remove(i);
                numDirLights--;
                continue;
            } else {
                i++;
            }
            
            Vector4f newDir = new Vector4f(light.dir, 0);
            view.transform(newDir);
            Utilities.putVector4f(buffer, newDir);
            Utilities.putVector3f(buffer, light.ambient);
            buffer.putFloat(0); //padding for vec4
            Utilities.putVector3f(buffer, light.diffuse);
            buffer.putFloat(0);//padding for vec4
            Utilities.putVector3f(buffer, light.specular);
            buffer.putFloat(0);//padding for vec4
        }
        buffer.position(0);
        buffer.putInt(numDirLights).putInt(0).putInt(0).putInt(0);//padding
        buffer.rewind();
        glBuffer.setChanged();
        
    }
    
    public int getNumLights() {
        return numDirLights;
        
    }
    
    public int getMaxNumLights() {
        return maxNumLights;
    }
    
    public void addDirLight(DirLight light) {
        dirLights.add(light);
        numDirLights++;
    }
    
    public void addDirLight(Vector3f dir, Vector3f ambient, Vector3f diffuse, Vector3f specular) {
        dirLights.add(new DirLight(dir, ambient, diffuse, specular));
        numDirLights++;
    }
    
    public static class DirLight {

        public Vector3f dir, ambient, diffuse, specular;
        private boolean remove;
        
        public DirLight(Vector3f dir, Vector3f ambient, Vector3f diffuse, Vector3f specular) {
            this.dir = dir;
            this.ambient = ambient;
            this.diffuse = diffuse;
            this.specular = specular;
        }
        
        public void remove() {
            remove = true;
        }
    }
    
   

}
