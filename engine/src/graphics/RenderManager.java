package graphics;

import io.GLFWManager;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
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
public class RenderManager {

    private static RenderManager instance;

    public static RenderManager getInstance() {
        if (instance == null) {
            instance = new RenderManager();
        }
        return instance;
    }

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
    //opengl buffers
    private List<GLBuffer> glBuffers;
    //buffers to upload
    private Queue<GLBuffer> buffersToUpload;

    private int resX, resY;

    public static final int HUD_Z_INDEX = 1000;
    public static final int DEFAULT_Z_INDEX = 0;
    public static final int PRE_RENDER_Z_INDEX = -1000;

    private RenderManager() {
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
        glBuffers = new ArrayList<>();
        buffersToUpload = new ConcurrentLinkedQueue<>();

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

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        resX = GLFWManager.getInstance().getResX();
        resY = GLFWManager.getInstance().getResY();

    }

    public int getResX() {
        return resX;
    }

    public int getResY() {
        return resY;
    }

    public void render() {
        prepareTextures();
        updateViewMat();
        updateBuffers();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

        Renderable renderableToAdd;
        while ((renderableToAdd = toAdd.poll()) != null) {
            initialize(renderableToAdd);
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

            bind(id);
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
        shaderProgram = sp;
        sp.update();
    }

    public void bind(int textureID) {
        bind(textureID, 0);
    }

    public void bind(int textureID, int textureUnit) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
        glBindTexture(GL_TEXTURE_2D, textureID);
    }

    public void bind(String name, int textureUnit) {
        bind(getTextureHandle(name), textureUnit);
    }

    public void unbind() {
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

    private void updateBuffers() {
        GLBuffer buf;
        while ((buf = buffersToUpload.poll()) != null) {
            buf.create();
            glBuffers.add(buf);
        }
        Iterator<GLBuffer> iter = glBuffers.iterator();
        while (iter.hasNext()) {
            buf = iter.next();
            if (buf.isToRelease()) {
                buf.destroy();
                iter.remove();
            } else if (buf.isChanged()) {
                buf.updateBuffer();
            }
        }
    }

    public void addGLBuffer(GLBuffer buf) {
            buffersToUpload.add(buf);
    }
    
    public void addUniformBuffer(String name, GLBuffer buf) {
        uniformBuffers.put(name, buf);
        addGLBuffer(buf);
    }
    
    public GLBuffer createUniformBuffer(String name, ByteBuffer data, boolean dynamic) {
        if (!uniformBuffers.containsKey(name)) {
            GLBuffer buf = new GLBuffer(GL31.GL_UNIFORM_BUFFER, dynamic ? GL15.GL_DYNAMIC_DRAW : GL15.GL_STATIC_DRAW, data);
            addUniformBuffer(name, buf);
            return buf;
        } else {
            System.err.println("buffer already created " + name);
            return uniformBuffers.get(name);
        }
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
