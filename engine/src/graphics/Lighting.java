package graphics;

import game.Component;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import static util.Utilities.putVector3f;
import static util.Utilities.putVector4f;

/**
 *
 * @author Andrew_2
 */
public class Lighting extends Renderable {

    public static final int MAX_LIGHTS = 16;
    
    private String name;
    private List<DirLight> dirLights;
    private int numDirLights;

    //the data for the uniform buffer "lightBlock"
    private ByteBuffer dirLightBuffer;

    public Lighting(Component parent, String name) {
        super(parent);
        
        this.name = name;
        dirLights = new ArrayList<>();
        dirLightBuffer = BufferUtils.createByteBuffer(4 * 4 * 4 * MAX_LIGHTS + 4 * 4); //lights + header
        numDirLights = 0;

        RenderManager.getInstance().createBuffer(name, dirLightBuffer, true);

    }
    
    @Override
    public int getZIndex() {
        return RenderManager.PRE_RENDER_Z_INDEX;
    }

    @Override
    public void render() {
        dirLightBuffer.rewind();
        dirLightBuffer.putInt(numDirLights).putInt(0).putInt(0).putInt(0);//padding
        Matrix4f view = RenderManager.getInstance().getViewMatrix();
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
            putVector4f(dirLightBuffer, newDir);
            putVector3f(dirLightBuffer, light.ambient);
            dirLightBuffer.putFloat(0); //padding for vec4
            putVector3f(dirLightBuffer, light.diffuse);
            dirLightBuffer.putFloat(0);//padding for vec4
            putVector3f(dirLightBuffer, light.specular);
            dirLightBuffer.putFloat(0);//padding for vec4
        }
        dirLightBuffer.rewind();
        RenderManager.getInstance().setUniformBuffer(name, dirLightBuffer);
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
