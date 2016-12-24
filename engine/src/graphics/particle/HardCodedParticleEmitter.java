package graphics.particle;

import game.Component;
import game.StandardGame;
import graphics.AttributeData;
import graphics.GLType;
import graphics.RenderManager;
import graphics.Renderable;
import graphics.ShaderProgram;
import graphics.UniformData;
import graphics.VAORender;
import java.nio.ByteBuffer;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;
import util.InterleavedBufferBuilder;

/**
 *
 * @author Andrew_2
 * 
 * A basic implementation of particle effects
 */
public class HardCodedParticleEmitter extends Renderable {

    String name;
    VAORender vao;
    AttributeData baseAttr;
    AttributeData dynamicAttr;
    InterleavedBufferBuilder ibb;

    int maxNumber;
    int currentNumber;
    int tmpLoc;
    int[] life;
    float[] sizes;
    float[] positions;
    float[] distances;
    float[] velocities;
    float[] colors;
    ShaderProgram shaderProgram;
    UniformData ud;
    int pHandle;
    int vHandle;
    
    float[] origin;

    private static final float[] baseData = {
        -1f, -1f, 0.0f,
        1f, -1f, 0.0f,
        -1f, 1f, 0.0f,
        1f, 1f, 0.0f
    };

    public HardCodedParticleEmitter(Component parent, String name, int maxNumber, ShaderProgram shaderProgram, Vector3f origin) {
        super(parent);
        this.maxNumber = maxNumber;
        this.shaderProgram = shaderProgram;
        this.name = name;
        this.origin = new float[]{origin.x, origin.y, origin.z};
        
        ud = new UniformData(shaderProgram);
        shaderProgram.setUniformData(ud);
        pHandle = ud.createUniform("proj", GLType.GL_m4fv, 1);
        vHandle = ud.createUniform("view_model", GLType.GL_m4fv, 1);
        
        ByteBuffer baseDataBuffer = BufferUtils.createByteBuffer(baseData.length * Float.BYTES);
        baseDataBuffer.asFloatBuffer().put(baseData);
        baseDataBuffer.rewind();
        
        vao = new VAORender(shaderProgram.getRenderManager());
        
        baseAttr = AttributeData.createAttributeData(vao, "base", GL15.GL_STATIC_DRAW);
        baseAttr.setData(baseDataBuffer);
        baseAttr.createAttribute("base", GLType.GL_3fv, 0, 12);
        
        currentNumber = maxNumber - 1;
        tmpLoc = maxNumber - 1;
        
        
        
        life = new int[maxNumber];
        distances = new float[maxNumber];
        velocities = new float[maxNumber * 3];
        
        ibb = new InterleavedBufferBuilder();
        ibb.setNumber(maxNumber);
        sizes = ibb.createFloatData(1);
        positions = ibb.createFloatData(3);
        colors = ibb.createFloatData(4);
        ibb.compile();
        
        dynamicAttr = AttributeData.createAttributeData(vao, "dynamic", GL15.GL_STREAM_DRAW, 1);
        dynamicAttr.setData(ibb.getData());
        dynamicAttr.createAttribute("isize", GLType.GL_1fv, 0, 32);
        dynamicAttr.createAttribute("center", GLType.GL_3fv, 4, 32);
        dynamicAttr.createAttribute("icolor", GLType.GL_4fv, 16, 32);
    }

    @Override
    public void initRender() {
        shaderProgram.createAndCompileShader();

        vao.generateVAO();
        vao.setShaderAttributeLocations(shaderProgram);
    }

    @Override
    public int getZIndex() {
        return 500;
    }

    @Override
    public void render() {
            updateParticles();
        
        shaderProgram.getRenderManager().useAndUpdateVAO(vao);
        
        ud.setUniform(pHandle, shaderProgram.getRenderManager().getProjectionMatrix());
        ud.setUniform(vHandle, shaderProgram.getRenderManager().getViewMatrix());
        shaderProgram.getRenderManager().useShaderProgram(shaderProgram);

        //GL11.glDepthMask(false);
        //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL31.glDrawArraysInstanced(GL11.GL_TRIANGLE_STRIP, 0, 4, currentNumber);
        //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        //GL11.glDepthMask(true);
    }

    public void updateParticles() {
        for (int i = 0; i < currentNumber; i++) {
            if (--life[i] <= 0) {
                createParticle(i);
            }
            sizes[i] = 0.015f * (float)Math.sqrt(life[i]);
            for (int j = 0; j < 3; j++) {
                positions[i * 3 + j] += velocities[i * 3 + j];
                velocities[i * 3 + j] *= .984f;
            }
            velocities[i * 3 + 1] -= 0.001f;
        }
        orderParticles();
        ibb.updateBuffer();
        dynamicAttr.setChanged();
    }

    public void createParticle(int i) {
        for (int j = 0; j < 3; j++) {
            positions[i * 3 + j] = 1 * ((float)Math.random() - .5f) + origin[j];
            velocities[i * 3 + j] = 0.12f * ((float) Math.random() - .5f);
            colors[i * 4 + j] = (float) Math.random();
        }
        life[i] = (int) (Math.random() * 200);
        //sizes[i] = (float)Math.random() / 10f;
        colors[i * 4 + 3] = (float)Math.random();
    }

    public static HardCodedParticleEmitter createEmit(Component parent,String name, int numParticles, StandardGame game) {
        return createEmit(parent, name, numParticles, new Vector3f(), game);
    }
    public static HardCodedParticleEmitter createEmit(Component parent,String name, int numParticles, Vector3f pos, StandardGame game) {
        ShaderProgram sp = ShaderProgram.loadProgram("shaders/particle.vs", "shaders/particle.fs", game);
        HardCodedParticleEmitter p = new HardCodedParticleEmitter(parent, name, numParticles, sp, pos);
        game.getRenderManager().add(p);
        return p;
    }
    
    private void orderParticles() {
        Vector3f cam = shaderProgram.getRenderManager().getViewPoint().getPosition();
        float[] cameraPos = {cam.x, cam.y, cam.z};
        for(int i = 0; i < currentNumber; i++) {
            distances[i] = 0;
            for(int j = 0; j < 3; j++) {
                float tmp = cameraPos[j] - positions[i * 3 + j];
                distances[i] += tmp * tmp;
            }
        }
        for(int i = 0; i < currentNumber; i++) {
            float dist = distances[i];
            moveData(i, tmpLoc);
            int j = i - 1;
            for(; j >= 0 && distances[j] < dist; j--) {
                moveData(j, j + 1);
            }
            
            moveData(tmpLoc, j + 1);
        }
    }

    private void moveData(int from, int to) {
        distances[to] = distances[from];
        life[to] = life[from];
        sizes[to] = sizes[from];
        for(int i = 0; i < 3; i++) {
            positions[to * 3 + i] = positions[from * 3 + i];
            velocities[to * 3 + i] = velocities[from * 3 + i];
        }
        for(int i = 0; i < 4; i++) {
            colors[to * 4 + i] = colors[from * 4 + i];
        }
        
        
    }

}
