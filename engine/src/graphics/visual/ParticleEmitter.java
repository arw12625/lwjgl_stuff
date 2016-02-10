package graphics.visual;

import game.Component;
import graphics.AttributeData;
import graphics.GLType;
import graphics.RenderManager;
import graphics.Renderable;
import graphics.ShaderProgram;
import graphics.UniformData;
import java.nio.ByteBuffer;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import org.lwjgl.opengl.GL31;

/**
 *
 * @author Andrew_2
 * 
 * A basic implementation of particle effects
 */
public class ParticleEmitter extends Renderable {

    String name;
    int arrayHandle;
    int baseHandle;
    int dynamicHandle;

    int maxNumber;
    int currentNumber;
    int[] life;
    float[] sizes;
    float[] positions;
    float[] velocities;
    float[] colors;
    ShaderProgram shaderProgram;
    UniformData ud;
    int pHandle;
    int vHandle;
    
    float[] origin;
    
    AttributeData baseAttr;
    AttributeData dynamicAttr;

    private static final float[] baseData = {
        -1f, -1f, 0.0f,
        1f, -1f, 0.0f,
        -1f, 1f, 0.0f,
        1f, 1f, 0.0f
    };

    public ParticleEmitter(Component parent, String name, int maxNumber, ShaderProgram shaderProgram, Vector3f origin) {
        super(parent);
        this.maxNumber = maxNumber;
        this.shaderProgram = shaderProgram;
        this.name = name;
        this.origin = new float[]{origin.x, origin.y, origin.z};
        
        ud = new UniformData(shaderProgram);
        shaderProgram.setUniformData(ud);
        pHandle = ud.createUniform("proj", GLType.GL_m4fv, 1);
        vHandle = ud.createUniform("view", GLType.GL_m4fv, 1);
        
        ByteBuffer baseDataBuffer = BufferUtils.createByteBuffer(baseData.length * Float.BYTES);
        baseDataBuffer.asFloatBuffer().put(baseData);
        baseDataBuffer.rewind();
        
        baseAttr = new AttributeData(shaderProgram, GL15.GL_ARRAY_BUFFER, GL15.GL_STATIC_DRAW);
        baseAttr.createGrouping(baseDataBuffer);
        baseAttr.createAttribute("base", GLType.GL_3fv, 0);

        RenderManager.getInstance().addGLBuffer(baseAttr);
        
        currentNumber = maxNumber;
        
        
        dynamicAttr = new AttributeData(shaderProgram, GL15.GL_ARRAY_BUFFER, GL15.GL_STREAM_DRAW);
        
        life = new int[maxNumber];
        velocities = new float[maxNumber * 3];
        
        dynamicAttr.createGrouping(maxNumber);
        sizes = dynamicAttr.createFloatAttribute("isize", GLType.GL_1fv, 1);
        positions = dynamicAttr.createFloatAttribute("center", GLType.GL_3fv, 1);
        colors = dynamicAttr.createFloatAttribute("icolor", GLType.GL_4fv, 1);
        
        RenderManager.getInstance().addGLBuffer(dynamicAttr);
    }

    @Override
    public void initRender() {
        shaderProgram.compileShader();

        arrayHandle = GL30.glGenVertexArrays();
        glBindVertexArray(arrayHandle);

        baseAttr.initialize();

        dynamicAttr.initialize();
    }

    @Override
    public int getZIndex() {
        return 500;
    }

    @Override
    public void render() {
        glBindVertexArray(arrayHandle);
        updateParticles();

        ud.setUniform(pHandle, RenderManager.getInstance().getProjectionMatrix());
        ud.setUniform(vHandle, RenderManager.getInstance().getViewMatrix());
        RenderManager.getInstance().useShaderProgram(shaderProgram);

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL31.glDrawArraysInstanced(GL11.GL_TRIANGLE_STRIP, 0, 4, currentNumber);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void updateParticles() {
        for (int i = 0; i < currentNumber; i++) {
            if (--life[i] <= 0) {
                createParticle(i);
            }
            sizes[i] = 0.01f * (float)Math.sqrt(life[i]);
            for (int j = 0; j < 3; j++) {
                positions[i * 3 + j] += velocities[i * 3 + j];
            }
        }
        dynamicAttr.setChanged();
    }

    public void createParticle(int i) {
        for (int j = 0; j < 3; j++) {
            positions[i * 3 + j] = (float)Math.random() + origin[j];
            velocities[i * 3 + j] = 0.03f * ((float) Math.random() - .5f);
            colors[i * 4 + j] = (float) Math.random();
        }
        life[i] = (int) (Math.random() * 200);
        //sizes[i] = (float)Math.random() / 10f;
        colors[i * 4 + 3] = (float)Math.random();
    }

    public static ParticleEmitter createEmit(Component parent,String name, int numParticles) {
        return createEmit(parent, name, numParticles, new Vector3f());
    }
    public static ParticleEmitter createEmit(Component parent,String name, int numParticles, Vector3f pos) {
        ShaderProgram sp = ShaderProgram.loadProgram("shaders/particle.vs", "shaders/particle.fs");
        ParticleEmitter p = new ParticleEmitter(parent, name, numParticles, sp, pos);
        RenderManager.getInstance().add(p);
        return p;
    }

}
