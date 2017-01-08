package graphics.particle;

import game.Component;
import game.StandardGame;
import graphics.AttributeData;
import graphics.GLType;
import graphics.RenderLayer;
import graphics.RenderManager;
import graphics.ShaderProgram;
import graphics.VAOAttributes;
import graphics.View;
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
public class TexturedParticleEmitter extends ParticleEmitter {

    ParticleDistribution pd;
    String texName;
    
    private static final int BYTESIZE = (1 + 3 + 2) * Float.BYTES;
    private static final int VERTS_PER_PART = 4;

    private static final String defaultTextureName = "misc_models/blueParticle.png";

    private static final int textureAtlasSize = 4;
    private static final float texOffsetUnit = 1f / textureAtlasSize;

    public TexturedParticleEmitter(String name,
            ShaderProgram sp, int capacity, ParticleDistribution pd, String texName) {
        super(name, sp, capacity, pd.getTransform());

        this.pd = pd;
        this.texName = texName;
    }

    @Override
    public void initParticles() {

        particles = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            particles.add(new TexturedParticle());
        }
    }

    @Override
    public void updateParticles(int delta) {
        pd.updateParticle(delta, particles);
        for(Object p : particles) {
            TexturedParticle tp = (TexturedParticle)p;

            int index = textureAtlasSize * textureAtlasSize - tp.life / 100 - 1;

            tp.texOffsetX = texOffsetUnit * (index % textureAtlasSize);
            tp.texOffsetY = texOffsetUnit * (index / textureAtlasSize);
        }
    }

    @Override
    public void renderInit() {
        super.renderInit();
        ud.setTexture("tex", texName);
    }
    
    @Override
    public void render(View view, RenderLayer layer) {
        super.render(view, layer);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL42.glDrawArraysInstancedBaseInstance(GL11.GL_TRIANGLE_STRIP, 0, 4, capacity, offset);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(true);
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
        -5f, -5f, 0.0f, 0, 0,
        5f, -5f, 0.0f, texOffsetUnit, 0,
        -5f, 5f, 0.0f, 0, texOffsetUnit,
        5f, 5f, 0.0f, texOffsetUnit, texOffsetUnit,
    };

    public static TexturedParticleEmitter createParticleEmitter(String name, int capacity, Vector3f origin, StandardGame game) {
        ShaderProgram shader = ShaderProgram.loadProgram("shaders/tex_particle.vs", "shaders/tex_particle.fs", game);
        TexturedParticleEmitter emit = new TexturedParticleEmitter(name, shader, capacity, new PointDistribution(), defaultTextureName);
        return emit;
    }

    public static ParticleEngine createParticleEngine(Component parent, String name, int capacity, StandardGame game) {
        VAOAttributes vao = new VAOAttributes(game.getRenderManager());

        resource.TextureData.loadTextureResource(defaultTextureName, game);

        ByteBuffer baseDataBuffer = BufferUtils.createByteBuffer(baseData.length * Float.BYTES);
        baseDataBuffer.asFloatBuffer().put(baseData);
        baseDataBuffer.rewind();

        AttributeData baseAttr = AttributeData.createAttributeData(vao, "base", GL15.GL_STATIC_DRAW);
        baseAttr.setData(baseDataBuffer);
        baseAttr.createAttribute("base", GLType.GL_3fv, 0, 20);
        baseAttr.createAttribute("vertex_tex_coord", GLType.GL_2fv, 12, 20);

        ByteBuffer dynamicData = BufferUtils.createByteBuffer(capacity * BYTESIZE * VERTS_PER_PART);
        AttributeData dynamicAttr = AttributeData.createAttributeData(vao, "dynamic", GL15.GL_STREAM_DRAW, 1);
        dynamicAttr.setData(dynamicData);
        dynamicAttr.createAttribute("isize", GLType.GL_1fv, 0, 24);
        dynamicAttr.createAttribute("center", GLType.GL_3fv, 4, 24);
        dynamicAttr.createAttribute("tex_offset", GLType.GL_2fv, 16,24);

        ParticleEngine engine = new ParticleEngine(name, vao);

        return engine;
    }

    private static class TexturedParticle extends SimpleParticle {

        float texOffsetX, texOffsetY;

        @Override
        public void write(ByteBuffer b) {
            b.putFloat(size);
            b.putFloat(position[0]).putFloat(position[1]).putFloat(position[2]);
            b.putFloat(texOffsetX).putFloat(texOffsetY);
        }

        @Override
        public int getSize() {
            return (1+3+2)*Float.BYTES;
        }
        
    }
}
