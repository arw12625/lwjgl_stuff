package graphics.visual;

import game.Component;
import geometry.Transform;
import graphics.AttributeData;
import graphics.GLType;
import graphics.RenderManager;
import graphics.Renderable;
import graphics.ShaderProgram;
import graphics.UniformData;
import graphics.UniformTransform;
import graphics.VAORender;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;

/**
 *
 * @author Andrew_2
 */
public class HeightMap extends Renderable {

    private int resx, resz, vertCount, indexCount;
    private float width, depth;
    private float[][] heights;
    private ByteBuffer coordData;
    private ByteBuffer indexData;
    private ByteBuffer dynamicData;
    
    VAORender vao;
    AttributeData staticAttr;
    AttributeData dynamicAttr;

    ShaderProgram shaderProgram;
    UniformData ud;
    Transform t;

    private static final int dynamicFloats = 4;
    private static final int heightOffset = 0;
    private static final int normalOffset = 1;
    
    public HeightMap(Component parent, float[][] heights, float width, float depth, Transform t, ShaderProgram shaderProgram) {
        super(parent);
        this.heights = heights;
        this.resx = heights[0].length;
        this.resz = heights.length;
        vertCount = resx * resz;
        indexCount = 2 * resx * resz - 2 * resx + resz;

        this.width = width;
        this.depth = depth;

        coordData = BufferUtils.createByteBuffer(vertCount * 2 * Float.BYTES);
        indexData = BufferUtils.createByteBuffer(indexCount * Integer.BYTES);
        dynamicData = BufferUtils.createByteBuffer(vertCount * dynamicFloats * Float.BYTES);
        
        updateBuffer();
        
        FloatBuffer floatView = coordData.asFloatBuffer();
        IntBuffer intView = indexData.asIntBuffer();
        
        for(int i = 0; i < resz; i++) {
            for(int j = 0; j < resx; j++) {
                floatView.put(j);
                floatView.put(i);
            }
        }
        floatView.rewind();
        
        int restartIndex = RenderManager.getInstance().getRestartIndex();
        
        for(int i = 0; i < resz - 1; i++) {
            for(int j = 0; j < resx; j++) {
                intView.put(i * resx + j);
                intView.put((i+1) * resx + j);
            }
            intView.put(restartIndex);
        }
        intView.rewind();
        
        vao = new VAORender();
        staticAttr = AttributeData.createAttributeData(vao, "static", GL15.GL_STATIC_DRAW);
        staticAttr.createAttribute("position", GLType.GL_2fv, 0, 8);
        staticAttr.setData(coordData);
        
        vao.createElementArray(GL15.GL_STATIC_DRAW, indexData);
       
        dynamicAttr = AttributeData.createAttributeData(vao, "dynamic", GL15.GL_DYNAMIC_DRAW);
        dynamicAttr.createAttribute("height", GLType.GL_1fv, 0, 16);
        dynamicAttr.createAttribute("vertex_normal", GLType.GL_3fv, 4, 16);
        dynamicAttr.setData(dynamicData);
        
        this.t = t;
        this.shaderProgram = shaderProgram;
        
        float tileWidth = width / resx;
        float tileDepth = depth / resz;
        Matrix4f initTrans = new Matrix4f();
        initTrans.scaling(tileWidth, 1, tileDepth);
        
        ud = new UniformData(shaderProgram);
        shaderProgram.setUniformData(ud);
        UniformTransform ut = new UniformTransform(t, initTrans);
        ud.addStruct(ut);
        ud.setUniformBuffer("lightBlock", "lightBlock");

    }

    @Override
    public void initRender() {
        shaderProgram.createAndCompileShader();

        vao.generateVAO();
        vao.setShaderAttributeLocations(shaderProgram);
    }
    
    @Override
    public void render() {
        RenderManager.getInstance().useAndUpdateVAO(vao);
        
        //dynamicAttr.setChanged();

        RenderManager.getInstance().useShaderProgram(shaderProgram);

        GL11.glEnable(GL31.GL_PRIMITIVE_RESTART);
        GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, indexCount, GL_UNSIGNED_INT, 0);
        GL11.glDisable(GL31.GL_PRIMITIVE_RESTART);
    }

    
    public static float[][] createWaveHeights(int resx, int resz) {
        
        float[][] heights = new float[resz][resx];
        
        for(int i = 0; i < resz; i++) {
            for(int j = 0; j < resx; j++) {
                heights[i][j] = (float)(0.1*Math.cos(7 + 0.006 * i + 0.004* j) + .2 * Math.cos(.1f + 0.032 * j) + .25 * Math.cos(0.5 + 0.02* i) + 0.08 * Math.cos(9 + 0.091 * i + 0.08 * j)+ 0.08 * Math.cos(9 + 0.091 * i - 0.08 * j));
            }
        }
        return heights;
    }
    
    
    public static float[][] createRandomHeights(int resx, int resz) {
        
        int blurRadius = 3;
        float[][] rand = new float[resz+2 * blurRadius][resx+2 * blurRadius];
        
        for(int i = 0; i < rand.length; i++) {
            for(int j = 0; j < rand[0].length; j++) {
                rand[i][j] = (float)Math.random()-.5f;
            }
        }
        
        float[][] heights = new float[resz][resx];
        
        for(int i = 0; i < resz; i++) {
            for(int j = 0; j < resx; j++) {
                for(int k = -blurRadius; k <= blurRadius; k++) {
                    for(int l = -blurRadius; l <= blurRadius; l++) {
                        heights[i][j] += rand[i + blurRadius + k][j + blurRadius + l] / (k*k + 1) / (l*l + 1);
                    }
                }
                heights[i][j] /= (blurRadius + 1) * (blurRadius + 1);
            }
        }
        
        return heights;
        
    }
    
    private void updateBuffer() {
        
        float[][][] normals = new float[resz][resx][3];
        for(int i = 0; i < resz - 1; i++) {
            for(int j = 0; j < resx - 1; j++) {
                float h0 = heights[i][j];
                float h1 = heights[i][j + 1];
                float h2 = heights[i + 1][j];
                float h3 = heights[i + 1][j + 1];
                
                float n0x = h0 - h1;
                float n0z = h0 - h2;
                float n1x = h2 - h3;
                float n1z = h1 - h3;
                
                float mag0 = (float)Math.sqrt(n0x * n0x + 1 + n0z * n0z);
                float mag1 = (float)Math.sqrt(n1x * n1x + 1 + n1z * n1z);
                
                float norm0x = n0x / mag0;
                float norm0y = 1 / mag0;
                float norm0z = n0z / mag0;
                
                float norm1x = n1x / mag1;
                float norm1y = 1 / mag1;
                float norm1z = n1z / mag1;
                
                normals[i][j][0] += norm0x;
                normals[i+1][j][0] += norm0x;
                normals[i][j+1][0] += norm0x;
                normals[i][j][1] += norm0y;
                normals[i+1][j][1] += norm0y;
                normals[i][j+1][1] += norm0y;
                normals[i][j][2] += norm0z;
                normals[i+1][j][2] += norm0z;
                normals[i][j+1][2] += norm0z;
                
                normals[i][j+1][0] += norm1x;
                normals[i+1][j][0] += norm1x;
                normals[i+1][j+1][0] += norm1x;
                normals[i][j+1][1] += norm1y;
                normals[i+1][j][1] += norm1y;
                normals[i+1][j+1][1] += norm1y;
                normals[i][j+1][2] += norm1z;
                normals[i+1][j][2] += norm1z;
                normals[i+1][j+1][2] += norm1z;
                
            }
        }
        
        FloatBuffer floatview = dynamicData.asFloatBuffer();
        for(int i = 0; i < resz; i++) {
            for(int j = 0; j < resx; j++) {
                floatview.put(heights[i][j]);
                float mag = 0;
                for(int k = 0; k < 3; k++) {
                    mag += normals[i][j][k] * normals[i][j][k];
                }
                mag = (float)Math.sqrt(mag);
                for(int k = 0; k < 3; k++) {
                    floatview.put(normals[i][j][k] / mag);
                }
            }
        }
        floatview.rewind();
        
    }
    
    public static void createHeightMap(Component parent, int resx, int resz, float width, float depth) {
        ShaderProgram sp = ShaderProgram.loadProgram("shaders/heightmap.vs", "shaders/heightmap.fs");
        float[][] heights = createWaveHeights(resx, resz);
        HeightMap hm = new HeightMap(parent, heights, width, depth, new Transform(parent), sp);
        RenderManager.getInstance().add(hm);
        
    }
    
}
