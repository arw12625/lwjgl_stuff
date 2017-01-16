package graphics.visual;

import game.Component;
import game.StandardGame;
import geometry.Transform;
import graphics.AttributeData;
import graphics.GLType;
import graphics.RenderLayer;
import graphics.RenderManager;
import graphics.ShaderProgram;
import graphics.UniformData;
import graphics.util.UniformTransform;
import graphics.VAOAttributes;
import graphics.View;
import graphics.util.GraphicsUtility;
import graphics.util.RenderableAdapter;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

/**
 *
 * @author Andrew_2
 */
public class SkyBox extends RenderableAdapter {

    VAOAttributes vao;
    AttributeData attr;

    ShaderProgram shaderProgram;
    UniformData ud;
    UniformTransform ut;

    private static final int[] cubeTriStripIndices = GraphicsUtility.getCubeTriStripIndices();
    private static final float[] cubeVerts = GraphicsUtility.getCubeVerts();
    
    private static final int numVert = cubeTriStripIndices.length;

    public SkyBox(ShaderProgram shader) {
        this.shaderProgram = shader;

        ByteBuffer coordData = BufferUtils.createByteBuffer(numVert * 3 * Float.BYTES);
        FloatBuffer floatView = coordData.asFloatBuffer();
        //iterate in reverse for as normals are inverted
        for(int i = cubeTriStripIndices.length - 1; i >= 0; i--) {
            int index = cubeTriStripIndices[i];
            floatView.put(cubeVerts[3*index]).put(cubeVerts[3*index+1]).put(cubeVerts[3*index+2]);
        }
        
        vao = new VAOAttributes(shaderProgram.getRenderManager());
        attr = AttributeData.createAttributeData(vao, "static", GL15.GL_DYNAMIC_DRAW);
        attr.createAttribute("position", GLType.GL_3fv, 0, 12);
        attr.setData(coordData);
        
        ud = new UniformData(shaderProgram);
        ut = new UniformTransform(new Transform(), null, false, true, false, true, false);
        ud.addStruct(ut);
        shaderProgram.setUniformData(ud);

    }

    public static SkyBox createSkyBox(StandardGame game) {
        ShaderProgram sp = ShaderProgram.loadProgram("shaders/skybox.vs", "shaders/skybox.fs", game);
        SkyBox sb = new SkyBox(sp);
        return sb;
    }
    
    @Override
    public void renderInit() {
        shaderProgram.createAndCompileShader();

        vao.generateVAO();
        vao.setShaderAttributeLocations(shaderProgram);
        
        setRenderInitialized();
    }

    @Override
    public void render(View view, RenderLayer layer) {
        vao.useAndUpdateVAO();
        
        ut.setCamera(GraphicsUtility.getHackyCamera(view));
        
        shaderProgram.useShaderProgram();

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, numVert);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
}
