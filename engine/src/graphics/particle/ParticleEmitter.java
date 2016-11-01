package graphics.particle;

import game.Component;
import geometry.Transform;
import graphics.AttributeData;
import graphics.RenderManager;
import graphics.Renderable;
import graphics.ShaderProgram;
import graphics.UniformData;
import graphics.UniformTransform;
import graphics.VAORender;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import util.BufferableHelper;

/**
 *
 * @author Andrew_2
 * @param <T>
 */
public abstract class ParticleEmitter<T extends Particle> extends Renderable {

    List<T> particles;
    BufferableHelper particleBuffer;
    
    VAORender vao;
    ShaderProgram shaderProgram;
    AttributeData dynamic;
    
    String name;
    int capacity;
    int offset;
    
    UniformData ud;
    UniformTransform ut;
    Transform t;
    int pHandle;
    int vHandle;
    
    public ParticleEmitter(Component parent, String name, 
            ShaderProgram sp, int capacity, Transform t) {
        super(parent);
        this.name = name;
        this.capacity = capacity;
        this.shaderProgram = sp;
        
        ud = new UniformData(shaderProgram);
        this.t = t;
        ut = new UniformTransform(t, null, false, true, false, true, false);
        ud.addStruct(ut);
      
        
    }
    
    
    public void init(VAORender vao, int offset) {
        
        this.vao = vao;
        this.offset = offset;
        
        dynamic = vao.getAttributeData("dynamic");
        ByteBuffer dynamicData = dynamic.getData();
        dynamicData.position(offset);
        
        dynamicData.limit(offset + getByteSize());
        ByteBuffer b = dynamicData.slice();
        b.order(ByteOrder.LITTLE_ENDIAN);
        dynamicData.clear();
        
        initParticles();
        
        particleBuffer = new BufferableHelper(b, particles);
    }
    
    public abstract void initParticles();
    
    public void update(int delta) {
        
        updateParticles(delta);

        particleBuffer.updateBuffer();

        dynamic.setChanged();
    }
    
    public abstract void updateParticles(int delta);
    
    
    public abstract int getVertexByteSize();
    public abstract int getVerticesPerParticle();
    
    public final int getParticleByteSize() {
        return getVertexByteSize() * getVerticesPerParticle();
    }
    public final int getByteSize() {
        return capacity * getParticleByteSize();
    }
    
    
    @Override
    public void render() {
        RenderManager.getInstance().useAndUpdateVAO(vao);
        shaderProgram.setUniformData(ud);
        RenderManager.getInstance().useShaderProgram(shaderProgram);
    
    }
    
    @Override
    public void initRender() {

        shaderProgram.createAndCompileShader();
        
        vao.setShaderAttributeLocations(shaderProgram);

    }
}
