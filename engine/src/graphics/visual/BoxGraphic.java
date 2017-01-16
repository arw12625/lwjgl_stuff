package graphics.visual;

import game.StandardGame;
import geometry.HasTransform;
import geometry.Transform;
import graphics.AttributeData;
import graphics.GLType;
import graphics.RenderLayer;
import graphics.RenderManager;
import graphics.ShaderProgram;
import graphics.UniformData;
import graphics.VAOAttributes;
import graphics.View;
import graphics.util.GraphicsUtility;
import graphics.util.RenderableAdapter;
import graphics.util.UniformTransform;
import java.nio.ByteBuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import org.lwjgl.opengl.GL15;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.ResourceManager;

/**
 *
 * @author Andrew_2
 */
public class BoxGraphic extends RenderableAdapter {

    private final int capacity;
    private boolean xray;
    private boolean wireframe;
    private final HasTransform[] boxTransforms;
    private int used;
    private final boolean[] enabled;
    private final ByteBuffer buffer;

    VAOAttributes vao;
    AttributeData attr;

    ShaderProgram shaderProgram;
    UniformData ud;
    UniformTransform ut;

    private static final float[] cubeVerts = GraphicsUtility.getCubeVerts();
    private static final int[] cubeQuadsIndices = GraphicsUtility.getCubeQuadsIndices();
    private static final int[] cubeLinesIndices = GraphicsUtility.getCubeLinesIndices();
    public static final int MAX_VERTS_PER_CUBE = Math.max(cubeQuadsIndices.length, cubeLinesIndices.length);

    public static final int BYTES_PER_VERT = 3 * Float.BYTES;

    public static final int NORMAL_Z_INDEX = RenderLayer.DEFAULT_INDEX;
    public static final int XRAY_Z_INDEX = RenderLayer.UI_INDEX - 1;

    private static final Logger LOG = LoggerFactory.getLogger(VectorGraphic.class);

    public BoxGraphic(int capacity, ShaderProgram sp) {
        this.capacity = capacity;
        this.shaderProgram = sp;

        boxTransforms = new HasTransform[capacity];
        enabled = new boolean[capacity];
        buffer = BufferUtils.createByteBuffer(capacity * MAX_VERTS_PER_CUBE * 3 * BYTES_PER_VERT);

        vao = new VAOAttributes(shaderProgram.getRenderManager());
        attr = AttributeData.createAttributeData(vao, "dynamic", GL15.GL_DYNAMIC_DRAW);
        attr.createAttribute("position", GLType.GL_3fv, 0, 12);
        attr.setData(buffer);

        ud = new UniformData(shaderProgram);
        ut = new UniformTransform(new Transform(), null, false, false, true, false, false);
        ud.addStruct(ut);
        shaderProgram.setUniformData(ud);
    }

    public static BoxGraphic createBoxGraphic(int capacity, StandardGame game) {
        return createBoxGraphic(capacity, game.getRenderManager(), game.getResourceManager());
    }

    public static BoxGraphic createBoxGraphic(int capacity, RenderManager renderManager, ResourceManager resourceManager) {

        ShaderProgram sp = ShaderProgram.loadProgram("shaders/vector.vs", "shaders/vector.fs",
                renderManager, resourceManager);
        BoxGraphic vg = new BoxGraphic(capacity, sp);
        return vg;
    }

    public static BoxGraphic createAndAddBoxGraphic(int capacity, StandardGame game, RenderLayer layer) {
        BoxGraphic vg = createBoxGraphic(capacity, game);
        layer.addRenderable(vg, XRAY_Z_INDEX);
        return vg;
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

        buffer.rewind();
        int length = wireframe ? cubeLinesIndices.length : cubeQuadsIndices.length;
        for (int i = 0; i < used; i++) {
            Vector4f tmp = new Vector4f();
            Matrix4f transform = boxTransforms[i].getTransformationMatrix(new Matrix4f());
            for (int j = 0; j < length; j++) {
                int index = wireframe ? cubeLinesIndices[j] : cubeQuadsIndices[j];
                tmp.set(cubeVerts[3 * index], cubeVerts[3 * index + 1], cubeVerts[3 * index + 2], 1);
                transform.transform(tmp);
                util.Utilities.putVector3fFrom4f(buffer, tmp);

            }
        }
        buffer.rewind();
        attr.setChanged();

        vao.useAndUpdateVAO();

        ut.setCamera(GraphicsUtility.getHackyCamera(view));

        shaderProgram.useShaderProgram();

        if (xray) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }
        if (wireframe) {
            glDrawArrays(GL11.GL_LINES, 0, 24 * used);
        } else {
            glDrawArrays(GL11.GL_QUADS, 0, 24 * used);
        }
        if (xray) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }

    }
    
    public void setXray(boolean xray) {
        this.xray = xray;
    }
    public void setWireframe(boolean wireframe) {
        this.wireframe = wireframe;
    }

    public int getAvailableHandle() {
        if (used == capacity) {
            LOG.error("Exceeded Capacity");
            return -1;
        } else {
            return used++;
        }
    }

    public int addBox(HasTransform t) {
        int handle = getAvailableHandle();
        setBox(handle, t);
        return handle;
    }

    public void setBox(int handle, HasTransform t) {
        boxTransforms[handle] = t;
    }

}
