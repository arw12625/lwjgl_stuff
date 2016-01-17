package graphics;

import io.GLFWManager;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
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
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import resource.Resource;
import resource.TextureData;

/**
 *
 * @author Andrew_2
 * 
 * RenderManager is a singleton manager
 * it interfaces with opengl, it runs on the main thread with opengl context
 * it contains a utility for creating gpu buffers for use by opengl
 * and maintains and renders a list of all Renderables
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

    public static final int MAX_LIGHTS = 16;
    private List<DirLight> lights;
    //the data for the uniform buffer "lightBlock"
    private ByteBuffer lightBuffer;
    private int numLights;

    //all named opengl buffers
    private Map<String, GLBuffer> glBuffers;
    //a queue of buffers with data to reupload
    private Queue<GLBuffer> buffersChanged;
    
    private int resX, resY;

    public static final int HUD_Z_INDEX = 1000;
    public static final int DEFAULT_Z_INDEX = 0;

    public static class GLBuffer {

        String name;
        ByteBuffer data;
        int bufferHandle;
        boolean dynamic;

        public GLBuffer(String name, boolean dynamic) {
            this.name = name;
            this.dynamic = dynamic;
            bufferHandle = -1;
        }
    }

    private RenderManager() {
        toAdd = new ConcurrentLinkedQueue<>();
        renderables = new ArrayList<>();
        zIndices = new ArrayList<>();

        projectionMatrix = new Matrix4f();
        projectionMatrix.identity();
        projectionViewMatrix = new Matrix4f();
        projectionViewMatrix.identity();
        lights = new ArrayList<>();
        lightBuffer = BufferUtils.createByteBuffer(4 * 4 * 4 * MAX_LIGHTS + 4 * 4); //lights + header
        numLights = 0;

        texturesToUpload = new ConcurrentLinkedQueue<>();
        texturesToUploadName = new ConcurrentLinkedQueue<>();
        textureHandles = new HashMap<>();

        glBuffers = new HashMap<>();
        buffersChanged = new ConcurrentLinkedQueue<>();

        Quaternionf q = new Quaternionf();
        q.set(new AxisAngle4f(0, 0, 0, 1));
        vp = new ViewPoint(new Vector3f(0, 0, 10), q);
        //setOrthographicProjection(16, 12, -.1f, -10);
        setPespectiveProjection(3.14f / 3, 1.333f, .1f, 100);

        createBuffer("lightBlock", lightBuffer, true);

        
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
        updateLights();
        GLBuffer buf;
        while ((buf = buffersChanged.poll()) != null) {
            if (buf.bufferHandle == -1) {
                buf.bufferHandle = glGenBuffers();
            }
            GL15.glBindBuffer(GL_UNIFORM_BUFFER, buf.bufferHandle);
            GL15.glBufferData(GL_UNIFORM_BUFFER, buf.data.capacity(), buf.data,
                    buf.dynamic ? GL15.GL_DYNAMIC_DRAW : GL15.GL_STATIC_DRAW);
        }

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

    public void useShaderProgram(ShaderProgram sp) {
        if(shaderProgram != sp) {
            shaderProgram = sp;
            sp.update();
        }
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
        projectionMatrix.mul(vp.getViewMatrix(), projectionViewMatrix);
        return projectionViewMatrix;
    }

    public Matrix4f getViewMatrix() {
        return vp.getViewMatrix();
    }

    public void createBuffer(String name, ByteBuffer data, boolean dynamic) {
        if (!glBuffers.containsKey(name)) {
            GLBuffer buf = new GLBuffer(name, dynamic);
            glBuffers.put(name, buf);
            setUniformBuffer(name, data);
        }
    }

    public void setUniformBuffer(String name, ByteBuffer data) {
        if (glBuffers.containsKey(name)) {
            GLBuffer buf = glBuffers.get(name);
            buf.data = data;
            if (!buffersChanged.contains(buf)) {
                buffersChanged.add(buf);
            }
        } else {
            System.err.println("no such buffer: " + name);
        }
    }

    public Integer getBufferHandle(String name) {
        if (glBuffers.containsKey(name)) {
            return glBuffers.get(name).bufferHandle;
        } else {
            System.err.println("no such buffer: " + name);
            return -1;
        }
    }

    public void updateLights() {
        lightBuffer.rewind();
        lightBuffer.putInt(numLights).putInt(0).putInt(0).putInt(0);
        for (int i = 0; i < numLights; i++) {
            Vector4f newDir = new Vector4f(lights.get(i).dir, 0);
            getViewMatrix().transform(newDir);
            putVector4f(lightBuffer, newDir);
            putVector3f(lightBuffer, lights.get(i).ambient);
            lightBuffer.putFloat(0);
            putVector3f(lightBuffer, lights.get(i).diffuse);
            lightBuffer.putFloat(0);
            putVector3f(lightBuffer, lights.get(i).specular);
            lightBuffer.putFloat(0);
        }
        lightBuffer.rewind();
        setUniformBuffer("lightBlock", lightBuffer);
    }

    public static void putVector4f(ByteBuffer b, Vector4f v) {
        b.putFloat(v.x).putFloat(v.y).putFloat(v.z).putFloat(v.w);
    }

    public static void putVector3f(ByteBuffer b, Vector3f v) {
        b.putFloat(v.x).putFloat(v.y).putFloat(v.z);
    }

    public void addLight(DirLight d) {
        numLights++;
        lights.add(d);
    }

    public static GLTYPE parseGLType(String type) {
        switch (type) {
            case "i":
                return GLTYPE.GLINT;
            case "d":
                return GLTYPE.GLDOUBLE;
            case "s":
                return GLTYPE.GLSHORT;
            case "b":
                return GLTYPE.GLBYTE;
            case "f":
            default:
                return GLTYPE.GLFLOAT;
        }
    }

    public enum GLTYPE {

        GLINT(4, GL11.GL_INT),
        GLDOUBLE(8, GL11.GL_DOUBLE),
        GLSHORT(2, GL11.GL_SHORT),
        GLBYTE(1, GL11.GL_BYTE),
        GLFLOAT(4, GL11.GL_FLOAT);

        private final int size;
        private final int glRef;

        GLTYPE(int size, int glRef) {
            this.size = size;
            this.glRef = glRef;
        }

        public int size() {
            return size;
        }

        public int glRef() {
            return glRef;
        }
    }

}
