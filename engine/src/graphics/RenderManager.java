package graphics;

import game.Game;
import io.Window;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import static org.lwjgl.opengl.GL20.glUseProgram;
import org.lwjgl.opengl.GL30;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import org.lwjgl.opengl.GL31;
import resource.Resource;
import resource.TextureData;

/**
 *
 * @author Andrew_2
 *
 * RenderManager is a singleton manager it interfaces with opengl, it runs on
 * the main thread with opengl context it contains a utility for creating gpu
 * buffers for use by opengl and maintains and renders a list of all Renderables
 * in addition it handles lighting and the viewpoint/camera
 *
 *
 */
public class RenderManager implements Runnable {

    //a queue of renderables added but not yet initialize for rendering
    private Queue<Renderable> toAdd;
    //a list of all objects to render sorted by z-index
    private List<Renderable> renderables;
    //the z-indices corresponding with the above renderables
    private List<Integer> zIndices;

    //the viewpoint and corresponding matrices for convienince
    private Matrix4f projectionMatrix;
    private Matrix4f projectionViewMatrix;
    private ViewPoint vp;

    //the last Vertex Array Object in use
    private VertexArrayObject vao;
    
    //the last shader program used
    private ShaderProgram shaderProgram;

    //a queue of textures to upload their buffers
    private Queue<TextureData> texturesToUpload;
    //the corresponding names
    private Queue<String> texturesToUploadName;
    //a map from texture name to texture handles used by opengl
    private Map<String, Integer> textureHandles;

    //named uniform opengl buffers
    private Map<String, GLBuffer> uniformBuffers;

    public static final int DEFAULT_WINDOW_WIDTH = 640;
    public static final int DEFAULT_WINDOW_HEIGHT = 480;
    
    public static final int HUD_Z_INDEX = 1000;
    public static final int DEFAULT_Z_INDEX = 0;
    public static final int PRE_RENDER_Z_INDEX = -1000;
    
    public static final int restartIndex = -1;
    
    public static final int RENDER_TIME = 1000 / 60;
    
    private Window window;
    private boolean toRelase;
    private boolean initialized;
    private boolean released;
    
    private static final Logger LOG = LoggerFactory.getLogger(RenderManager.class);


    public RenderManager(Window window) {
        LOG.info("RenderManager constructor entered");
        toAdd = new ConcurrentLinkedQueue<>();
        renderables = new ArrayList<>();
        zIndices = new ArrayList<>();

        projectionMatrix = new Matrix4f();
        projectionMatrix.identity();
        projectionViewMatrix = new Matrix4f();
        projectionViewMatrix.identity();

        texturesToUpload = new ConcurrentLinkedQueue<>();
        texturesToUploadName = new ConcurrentLinkedQueue<>();
        textureHandles = new HashMap<>();

        uniformBuffers = new HashMap<>();

        this.window = window;
        LOG.info("RenderManager constructor exited");
        
    }
    
    public void initialize() {
        
        LOG.info("RenderManager init entered");
        
        Quaternionf q = new Quaternionf();
        q.set(new AxisAngle4f(0, 0, 0, 1));
        vp = new ViewPoint(new Vector3f(0, 0, 10), q);
        //setOrthographicProjection(16, 12, -.1f, -10);
        setPespectiveProjection(3.14f / 3, 1.333f, .1f, 100);

        //opengl settings
        glClearColor(.15f, 0.15f, .15f, 0);
        GL11.glClearDepth(1f);
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        GL31.glPrimitiveRestartIndex(restartIndex);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        
        
        initialized = true;
        
        LOG.info("RenderManager init exited");
    }
    
    public int getWindowWidth() {
        return window.getWidth();
    }

    public int getWindowHeight() {
        return window.getHeight();
    }
    
    @Override
    public void run() {
        
        LOG.info(Game.threadMarker, "Render");
        LOG.info("RenderManager run");
        
        
        //the window must already be initialized
        //bind the opengl context of the window to the current thread
        window.bindGLContext();
        
        initialize();
        
        while(!toRelase) {
            render();
            window.swapBuffers();
            try {
                Thread.sleep(RENDER_TIME);
            } catch (InterruptedException ex) {
                LOG.error("{}", ex);
            }
        }
        
        released = true;

    }
    
    public boolean isInitialized() {
        return initialized;
    }

    public void render() {
        prepareTextures();
        updateViewMat();
        updateUniformBuffers();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

        Renderable renderableToAdd;
        while ((renderableToAdd = toAdd.poll()) != null) {
            RenderManager.this.initialize(renderableToAdd);
        }

        int i = 0;
        while (i < renderables.size()) {
            Renderable r = renderables.get(i);
            //destroyed renderables are removed
            if (r.isDestroyed()) {
                remove(i);
            } else if (r.isEnabled()) {
                r.render();
            }
                i++;
        }
        
    }
    
    public void release() {
        LOG.info("RenderManager release entered");
        toRelase = true;
        while(!released) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                LOG.error("{}", ex);
            }
        }
        LOG.info("RenderManager release exited");
    }

    public void add(Renderable r) {
        toAdd.add(r);
    }

    private void initialize(Renderable r) {
        r.internalInit();

        int i = 0;
        int z = r.getZIndex();
        while (i < renderables.size() && zIndices.get(i) < z) {
            i++;
        }
        renderables.add(i, r);
        zIndices.add(i, z);
    }

    public void remove(Renderable r) {
        toAdd.remove(r);
        int i = renderables.indexOf(r);
        remove(i);
    }

    private void remove(int index) {
        if (index < renderables.size() && index >= 0) {
            renderables.remove(index);
            zIndices.remove(index);
        }
    }

    public String queueTexture(String name, TextureData tr) {
        texturesToUpload.add(tr);
        texturesToUploadName.add(name);
        return name;
    }

    public String queueTexture(Resource<TextureData> tr) {
        return queueTexture(tr.getPath(), tr.getData());
    }

    private void prepareTextures() {
        TextureData texture;
        while ((texture = texturesToUpload.poll()) != null) {
            String name = texturesToUploadName.poll();
            int id = GL11.glGenTextures();
            textureHandles.put(name, id);

            RenderManager.this.bindTexture(id);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            glTexImage2D(GL_TEXTURE_2D, 0, texture.getType().getDataType(), texture.getImageWidth(), texture.getImageHeight(),
                    0, texture.getType().getGLType(), GL_UNSIGNED_BYTE, texture.getBuffer());
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            GL30.glGenerateMipmap(GL_TEXTURE_2D);
        }
    }

    //must be called in a thread with opengl context
    public void useShaderProgram(ShaderProgram sp) {
        if(shaderProgram != sp) {
            shaderProgram = sp;
            glUseProgram(sp.getProgram());
        }
        sp.update();
    }
    //must be called in a thread with opengl context
    public void useVAO(VertexArrayObject vao) {
        if(this.vao != vao) {
            this.vao=vao;
            glBindVertexArray(vao.getHandle());
        }
    }
    
    //must be called in a thread with opengl context
    public void useAndUpdateVAO(VertexArrayObject vao) {
        useVAO(vao);
        vao.update();
    }

    public void bindTexture(int textureID) {
        bindTexture(textureID, 0);
    }

    public void bindTexture(int textureID, int textureUnit) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
        glBindTexture(GL_TEXTURE_2D, textureID);
    }

    public void bind(String name, int textureUnit) {
        bindTexture(getTextureHandle(name), textureUnit);
    }

    public void unbindTexture() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public int getTextureHandle(String name) {
        return textureHandles.getOrDefault(name, 0);
    }

    public void updateViewMat() {
        projectionMatrix.mul(vp.getViewMatrix(), projectionViewMatrix);
    }

    public void setOrthographicProjection(float width, float height, float near, float far) {
        projectionMatrix.set(2 / width, 0, 0, 0,
                0, 2 / height, 0, 0,
                0, 0, -2 / (far - near), (far + near) / (far - near),
                0, 0, 0, 1);
        projectionMatrix.transpose();
    }

    public void setPespectiveProjection(float fovx, float aspect, float znear, float zfar) {
        projectionMatrix.setPerspective(fovx / aspect, aspect, znear, zfar);
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public ViewPoint getViewPoint() {

        return vp;
    }

    public Matrix4f getProjectionViewMatrix() {
        return projectionViewMatrix;
    }

    public Matrix4f getViewMatrix() {
        return vp.getViewMatrix();
    }

    public GLBuffer getUniformBuffer(String name) {
        return uniformBuffers.get(name);
    }

    private void updateUniformBuffers() {
        Iterator<GLBuffer> iter = uniformBuffers.values().iterator();
        while(iter.hasNext()) {
            GLBuffer buf = iter.next();
            buf.updateBuffer();
        }
    }
    
    public void addUniformBuffer(String name, GLBuffer buf) {
        uniformBuffers.put(name, buf);
    }
    
    public GLBuffer createUniformBuffer(String name, ByteBuffer data, boolean dynamic) {
        if (!uniformBuffers.containsKey(name)) {
            GLBuffer buf = new GLBuffer(GL31.GL_UNIFORM_BUFFER, dynamic ? GL15.GL_DYNAMIC_DRAW : GL15.GL_STATIC_DRAW, data, this);
            addUniformBuffer(name, buf);
            return buf;
        } else {
            LOG.warn("Attempted to create a UniformBuffer with a preexisting name: {}", name);
            return uniformBuffers.get(name);
        }
    }
    
    public Window getWindow() {
        return window;
    }
    
    public int getRestartIndex() {
        return restartIndex;
    }

    public static int parseGLType(String type) {
        switch (type) {
            case "i":
                return GL_INT;
            case "d":
                return GL_DOUBLE;
            case "s":
                return GL_SHORT;
            case "b":
                return GL_BYTE;
            case "f":
            default:
                return GL_FLOAT;
        }
    }


}
