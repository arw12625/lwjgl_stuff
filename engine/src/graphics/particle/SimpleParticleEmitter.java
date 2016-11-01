package graphics.particle;

import game.Component;
import graphics.AttributeData;
import graphics.GLType;
import graphics.RenderManager;
import graphics.ShaderProgram;
import graphics.VAORender;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL42;
import update.UpdateManager;

/**
 *
 * @author Andrew_2
 */
public class SimpleParticleEmitter extends ParticleEmitter {

    private static final int BYTESIZE = (1 + 3 + 4) * Float.BYTES;
    private static final int VERTS_PER_PART = 4;
    
    ParticleDistribution pd;

    public SimpleParticleEmitter(Component parent, String name,
            ShaderProgram sp, int capacity, ParticleDistribution pd) {
        super(parent, name, sp, capacity, pd.getTransform());

        this.pd = pd;
    }

    @Override
    public void initParticles() {

        particles = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            particles.add(new SimpleParticle());
        }
    }

    @Override
    public void updateParticles(int delta) {

        pd.updateParticle(delta, particles);

    }

    @Override
    public void render() {
        super.render();
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL42.glDrawArraysInstancedBaseInstance(GL11.GL_TRIANGLE_STRIP, 0, VERTS_PER_PART, capacity, offset);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(true);
    }

    @Override
    public int getZIndex() {
        return 500;
    }

    @Override
    public int getVertexByteSize() {
        return BYTESIZE;
    }

    @Override
    public int getVerticesPerParticle() {
        return VERTS_PER_PART;
    }

    private static final float[] baseData = {
        -1f, -1f, 0.0f,
        1f, -1f, 0.0f,
        -1f, 1f, 0.0f,
        1f, 1f, 0.0f
    };

    public static SimpleParticleEmitter createParticleEmitter(Component parent, String name, int capacity, Vector3f origin, ParticleEngine engine) {
        ShaderProgram shader = ShaderProgram.loadProgram("shaders/particle.vs", "shaders/particle.fs");
        SimpleParticleEmitter emit = new SimpleParticleEmitter(parent, name, shader, capacity, new PointDistribution());
        engine.addParticleEmitter(emit);
        RenderManager.getInstance().add(emit);
        return emit;
    }

    public static ParticleEngine createParticleEngine(Component parent, String name, int capacity) {
        VAORender vao = new VAORender();

        ByteBuffer baseDataBuffer = BufferUtils.createByteBuffer(baseData.length * Float.BYTES);
        baseDataBuffer.asFloatBuffer().put(baseData);
        baseDataBuffer.rewind();

        AttributeData baseAttr = AttributeData.createAttributeData(vao, "base", GL15.GL_STATIC_DRAW);
        baseAttr.setData(baseDataBuffer);
        baseAttr.createAttribute("base", GLType.GL_3fv, 0, 12);

        ByteBuffer dynamicData = BufferUtils.createByteBuffer(capacity * BYTESIZE * VERTS_PER_PART);
        AttributeData dynamicAttr = AttributeData.createAttributeData(vao, "dynamic", GL15.GL_STREAM_DRAW, 1);
        dynamicAttr.setData(dynamicData);
        dynamicAttr.createAttribute("isize", GLType.GL_1fv, 0, 32);
        dynamicAttr.createAttribute("center", GLType.GL_3fv, 4, 32);
        dynamicAttr.createAttribute("icolor", GLType.GL_4fv, 16, 32);

        ParticleEngine engine = new ParticleEngine(parent, name, vao);
        RenderManager.getInstance().add(engine);
        UpdateManager.getInstance().add(engine);

        return engine;
    }

}
