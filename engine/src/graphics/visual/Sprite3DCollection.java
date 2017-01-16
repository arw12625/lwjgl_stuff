package graphics.visual;

import game.StandardGame;
import geometry.HasTransform;
import geometry.Transform;
import graphics.AttributeData;
import graphics.GLType;
import graphics.RenderLayer;
import graphics.RenderManager;
import graphics.ShaderProgram;
import graphics.UniformBuffer;
import graphics.UniformData;
import graphics.VAOAttributes;
import graphics.View;
import graphics.util.GraphicsUtility;
import graphics.util.RenderableAdapter;
import graphics.util.UniformTransform;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.Resource;
import resource.TextureData;
import util.Bufferable;
import util.BufferableHelper;
import util.Utilities;

/**
 *
 * @author Andrew_2
 */
public class Sprite3DCollection extends RenderableAdapter {
    
    public static class Sprite3D implements Bufferable {
        
        private final Vector3f[] vertices;
        private final Vector3f[] normals;
        private final Vector2f[] texCoords;
        private final Vector4f[] colors;
        private String texName;
        private boolean useTex;
        private boolean enabled;
        private boolean changed;
        
        private static final Vector3f[] rectVerts = {
            new Vector3f(-1,1,0), new Vector3f(1,1,0), 
            new Vector3f(1,-1,0), new Vector3f(-1,-1,0)};
        private static final Vector3f rectNorm = new Vector3f(0,0,1);
        private static final Vector2f[] rectTexCoords = {
            new Vector2f(0,0), new Vector2f(1,0), 
            new Vector2f(1,1), new Vector2f(0,1)};
        
        public Sprite3D() {
            vertices = new Vector3f[VERTS_PER_SPRITE];
            normals = new Vector3f[VERTS_PER_SPRITE];
            texCoords = new Vector2f[VERTS_PER_SPRITE];
            colors = new Vector4f[VERTS_PER_SPRITE];
            for(int i = 0; i < VERTS_PER_SPRITE; i++) {
                vertices[i] = new Vector3f();
                normals[i] = new Vector3f();
                texCoords[i] = new Vector2f();
                colors[i] = new Vector4f();
            }
        }
        
        @Override
        public int getBytes() {
            return BYTES_PER_SPRITE;
        }
        
        @Override
        public void write(ByteBuffer b) {
            for (int i = 0; i < 4; i++) {
                Utilities.putVector3f(b, vertices[i]);
                Utilities.putVector3f(b, normals[i]);
                Utilities.putVector2f(b, texCoords[i]);
                Utilities.putVector4f(b, colors[i]);
                b.putInt(useTex ? 1 : 0);
            }
        }
        
        public void enable(boolean ena) {
            this.enabled = ena;
        }
        
        public void useTexture(String texName) {
            this.texName = texName;
        }
        
        public void enableTexture(boolean enableTex) {
            this.useTex = enableTex;
        }
        
        public void setColor(Vector4f color) {
            for(int i = 0; i < VERTS_PER_SPRITE; i++) {
                colors[i] = color;
            }
        }
        
        public void setRectangle(Vector3f pos, Quaternionf orientation) {
            
            for(int i = 0; i < VERTS_PER_SPRITE; i++) {
                vertices[i].set(rectVerts[i]);
                normals[i].set(rectNorm);
                texCoords[i].set(rectTexCoords[i]);
                
                orientation.transform(vertices[i]);
                vertices[i].add(pos);
                orientation.transform(normals[i]);
            }
            changed = true;
            enabled = true;
        }
        
    }
    
    private Sprite3D[] sprites;
    private int capacity;
    private int numUsed;
    
    private VAOAttributes vao;
    private AttributeData attr;
    private ShaderProgram sp;
    private UniformData ud;
    private UniformTransform ut;
    private UniformBuffer lighting;
    private ByteBuffer buffer;
    
    public static final int MAX_NUMBER = 100;
    public static final int VERTS_PER_SPRITE = 4;
    public static final int BYTES_PER_VERT = (1 + 3 + 3 + 2 + 4) * Float.BYTES;
    public static final int BYTES_PER_SPRITE = VERTS_PER_SPRITE * BYTES_PER_VERT;
    
    private static final Logger LOG = LoggerFactory.getLogger(Sprite3DCollection.class);
    
    public Sprite3DCollection(ShaderProgram shaderProgram, UniformBuffer lighting) {
        this(MAX_NUMBER, shaderProgram, lighting);
    }
    
    public Sprite3DCollection(int capacity, ShaderProgram shaderProgram, UniformBuffer lighting) {
        this.capacity = capacity;
        sprites = new Sprite3D[capacity];
        buffer = BufferUtils.createByteBuffer(capacity * BYTES_PER_SPRITE);
        this.lighting = lighting;
        
        vao = new VAOAttributes(shaderProgram.getRenderManager());
        attr = AttributeData.createAttributeData(vao, "texture", GL_DYNAMIC_DRAW);
        attr.setData(buffer);
        attr.createAttribute("vertex_position", GLType.GL_3fv, 0, BYTES_PER_VERT);
        attr.createAttribute("vertex_normal", GLType.GL_3fv, 3 * Float.BYTES, BYTES_PER_VERT);
        attr.createAttribute("vertex_tex_coord", GLType.GL_2fv, (3 + 3) * Float.BYTES, BYTES_PER_VERT);
        attr.createAttribute("vertex_color", GLType.GL_4fv, (3 + 3 + 2) * Float.BYTES, BYTES_PER_VERT);
        attr.createAttribute("vertex_use_tex", GLType.GL_1iv, (3 + 3 + 2 + 4) * Float.BYTES, BYTES_PER_VERT);
        
        this.sp = shaderProgram;
        //sp = ShaderProgram.loadProgram("shaders/flatTexture.vs", "shaders/texture.fs");
        ud = new UniformData(sp);
        sp.setUniformData(ud);
        ut = new UniformTransform(new Transform());
        ud.addStruct(ut);
    }
    
    public static Sprite3DCollection createSprite3DCollection(int capacity, StandardGame game, UniformBuffer lighting) {
        ShaderProgram defaultShader = ShaderProgram.loadProgram("shaders/sprite3d.vs", "shaders/sprite3d.fs", game);
        return new Sprite3DCollection(capacity, defaultShader, lighting);
    }
    
    public Sprite3D createSprite() {
        if(numUsed < capacity) {
            Sprite3D s = new Sprite3D();
            sprites[numUsed] = s;
            numUsed++;
            return s;
        } else {
            LOG.error("Exceeded capacity");
            return null;
        }
    }
    
    @Override
    public void renderInit() {
        
        sp.createAndCompileShader();
        
        vao.generateVAO();
        vao.setShaderAttributeLocations(sp);
        
        setRenderInitialized();
    }
    
    @Override
    public void render(View v, RenderLayer layer) {
        boolean attrChanged = false;
        for (int i = 0; i < numUsed; i++) {
            if (sprites[i].changed) {
                attrChanged = true;
            }
        }
        if(attrChanged) {
            buffer.rewind();
            for (int i = 0; i < numUsed; i++) {
                sprites[i].write(buffer);
            }
            buffer.rewind();
            attr.setChanged();
        }
        
        vao.useAndUpdateVAO();
        
        ud.setUniformBuffer("lightBlock", lighting.getGLBuffer(v));
        ut.setCamera(GraphicsUtility.getHackyCamera(v));
        
        for (int i = 0; i < numUsed; i++) {
            Sprite3D s = sprites[i];
            if (s.enabled) {
                
                if(s.useTex && s.texName != null) {
                    String texName = s.texName;
                    ud.setTexture("tex", texName);
                }
                sp.useShaderProgram();
                
                glDrawArrays(GL_QUADS, i * 4, 4);
            }
        }
    }
    
}
