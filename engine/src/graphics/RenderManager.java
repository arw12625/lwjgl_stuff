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
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import util.ZIndexSet;
import util.ZIndexSetStandard;

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

    //the window this instance manages
    private Window window;
    private boolean toRelase;
    private boolean initialized;
    private boolean released;

    //A list of all view associated with this instance
    private Set<View> views;
    //A list of ordered layers
    private ZIndexSet<RenderLayer> layers;

    //the current Vertex Array Object in use
    private VAO vao;

    //the last shader program used
    private ShaderProgram shaderProgram;

    //a queue of textures to upload their buffers
    private Queue<TextureData> texturesToUpload;
    //the corresponding names
    private Queue<String> texturesToUploadName;
    //a map from texture name to texture handles used by opengl
    private Map<String, Integer> textureHandles;

    public static final int restartIndex = -1;

    public static final int RENDER_TIME = 1000 / 60;

    private static final Logger LOG = LoggerFactory.getLogger(RenderManager.class);

    public RenderManager(Window window) {
        LOG.info("RenderManager constructor entered");

        views = new CopyOnWriteArraySet<>();
        layers = ZIndexSetStandard.<RenderLayer>createCopyOnWriteSet();

        texturesToUpload = new ConcurrentLinkedQueue<>();
        texturesToUploadName = new ConcurrentLinkedQueue<>();
        textureHandles = new HashMap<>();

        this.window = window;
        LOG.info("RenderManager constructor exited");

    }

    public void addView(View view) {
        views.add(view);
    }

    public void addRenderLayer(RenderLayer layer, int index) {
        layers.add(layer, index);
    }

    public void initialize() {

        LOG.info("RenderManager init entered");

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

        while (!toRelase) {
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
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

        
        prepareTextures();

        layers.sort();
        Iterator<RenderLayer> layerIterator = layers.iterator();
        while (layerIterator.hasNext()) {
            RenderLayer layer = layerIterator.next();
            Iterator<View> viewIterator = views.iterator();
            while (viewIterator.hasNext()) {
                View view = viewIterator.next();

                if (view.supportsLayer(layer)) {
                    view.refresh(layer);
                    layer.render(view);
                }
            }
        }

    }

    public void release() {
        LOG.info("RenderManager release entered");
        toRelase = true;
        while (!released) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                LOG.error("{}", ex);
            }
        }
        LOG.info("RenderManager release exited");
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
        if (shaderProgram != sp) {
            shaderProgram = sp;
            glUseProgram(sp.getProgram());
        }
        sp.update();
    }

    //must be called in a thread with opengl context
    public void useVAO(VAO vao) {
        if (this.vao != vao) {
            this.vao = vao;
            glBindVertexArray(vao.getHandle());
        }
    }

    //must be called in a thread with opengl context
    public void useAndUpdateVAO(VAO vao) {
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
