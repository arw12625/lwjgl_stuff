package graphics.visual;

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
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import org.lwjgl.opengl.GL15;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.ResourceManager;

/**
 *
 * @author Andy
 *
 * VectorGraphic enables the display of a vector with world location and
 * direction Not tested
 *
 */
public class VectorGraphic extends RenderableAdapter {

    private final int capacity;
    private final boolean xray;
    private final Vector3f[] vectors;
    private int used;
    private final boolean[] enabled;
    private final ByteBuffer buffer;
    private boolean isChanged;

    VAOAttributes vao;
    AttributeData attr;

    ShaderProgram shaderProgram;
    UniformData ud;
    UniformTransform ut;

    public static final int NUM_BYTES = 2 * 3 * Float.BYTES;

    public static final int NORMAL_Z_INDEX = RenderLayer.DEFAULT_INDEX;
    public static final int XRAY_Z_INDEX = RenderLayer.UI_INDEX - 1;
    
    private static final Logger LOG = LoggerFactory.getLogger(VectorGraphic.class);

    public VectorGraphic(int capacity, boolean xray,
            ShaderProgram sp) {
        this.capacity = capacity;
        this.xray = xray;
        this.shaderProgram = sp;

        vectors = new Vector3f[2 * capacity];
        enabled = new boolean[capacity];
        buffer = BufferUtils.createByteBuffer(2*capacity * NUM_BYTES);

        vao = new VAOAttributes(shaderProgram.getRenderManager());
        attr = AttributeData.createAttributeData(vao, "dynamic", GL15.GL_DYNAMIC_DRAW);
        attr.createAttribute("position", GLType.GL_3fv, 0, 12);
        attr.setData(buffer);

        ud = new UniformData(shaderProgram);
        ut = new UniformTransform(new Transform(), null, false, false, true, false, false);
        ud.addStruct(ut);
        shaderProgram.setUniformData(ud);
    }

    public static VectorGraphic createVectorGraphic(int capacity, boolean xray, StandardGame game) {
        return createVectorGraphic(capacity, xray, game.getRenderManager(), game.getResourceManager());
    }

    public static VectorGraphic createVectorGraphic(int capacity, boolean xray, RenderManager renderManager, ResourceManager resourceManager) {

        ShaderProgram sp = ShaderProgram.loadProgram("shaders/vector.vs", "shaders/vector.fs",
                renderManager, resourceManager);
        VectorGraphic vg = new VectorGraphic(capacity, xray, sp);
        return vg;
    }

    
    public static VectorGraphic createAndAddVectorGraphic(int capacity, boolean xray, StandardGame game, RenderLayer layer) {
        VectorGraphic vg = createVectorGraphic(capacity, xray, game);
        layer.addRenderable(vg, xray ? XRAY_Z_INDEX : NORMAL_Z_INDEX);
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

        if (isChanged) {
            buffer.rewind();
            for (int i = 0; i < 2*used; i++) {
                util.Utilities.putVector3f(buffer, vectors[i]);
            }
            buffer.rewind();
            attr.setChanged();
            isChanged = false;
        }
        vao.useAndUpdateVAO();

        ut.setCamera(GraphicsUtility.getHackyCamera(view));

        shaderProgram.useShaderProgram();

        if (xray) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }
        for (int i = 0; i < capacity; i++) {
            if(enabled[i]) {
                glDrawArrays(GL11.GL_LINES, i * 2, 2);
            }
        }
        if (xray) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }

    }

    public int getAvailableHandle() {
        if(used == capacity) {
            LOG.error("Exceeded Capacity");
            return -1;
        } else {
            return used++;
        }
    }

    public int addVector(Vector3f start, Vector3f end) {
        int handle = getAvailableHandle();
        setVector(handle, start, end);
        enableVector(handle, true);
        return handle;
    }
    
    public void enableVector(int handle, boolean enable) {
        enabled[handle] = enable;
    }
    
    public void setVector(int handle, Vector3f start, Vector3f end) {
        vectors[2*handle] = start;
        vectors[2*handle+1] = end;
        isChanged = true;
    }

}
