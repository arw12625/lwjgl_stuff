package graphics.visual;

import game.Component;
import resource.JSONData;
import geometry.Material;
import geometry.Transform;
import graphics.RenderManager;
import graphics.Renderable;
import graphics.ShaderProgram;
import graphics.UniformData;
import graphics.UniformTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import org.lwjgl.opengl.GL20;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import org.lwjgl.opengl.GL30;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import resource.ResourceManager;
import resource.TextureData;
import update.UpdateManager;

/**
 *
 * @author Andrew_2
 *
 * JSONRenderer interprets and renders data from the custom JSON model format
 * the model format allows multiple meshes with individual materials and shaders
 * added functionality may be created by subclassing JSONRenderer additionally
 * uniforms may be set by referencing the UniformData of each mesh
 * 
 * meshes rendered using indexed data
 *
 */
public class JSONRenderer extends Renderable {

    private JSONData model;
    private Map<String, Material> materials;
    private List<Mesh> meshes;
    private List<ShaderProgram> shaders;

    public JSONRenderer(Component parent, JSONData model) {
        super(parent);
        this.model = model;
        materials = new HashMap<>();
        meshes = new ArrayList<>();
        shaders = new ArrayList<>();
        JSONObject materialsJSON = model.getJSON().getJSONObject("materials");
        Iterator<String> matIt = materialsJSON.keys();
        while (matIt.hasNext()) {
            String matName = matIt.next();
            Material mat = Material.fromJSON(materialsJSON.getJSONObject(matName));

            Map<String, String> textureMap = mat.getTextureMap();
            for (String value : textureMap.values()) {
                TextureData.loadTextureResource(value);
            }
            materials.put(matName, mat);
        }

        JSONArray jsonShaders = model.getJSON().getJSONArray("shaders");
        for (int i = 0; i < jsonShaders.length(); i++) {
            shaders.add(ShaderProgram.loadProgram("shaders/" + jsonShaders.getJSONObject(i).getString("vertex_shader"),
                    "shaders/" + jsonShaders.getJSONObject(i).getString("fragment_shader")));
        }
        JSONArray jsonMeshes = model.getJSON().getJSONArray("meshes");
        
        Transform t = new Transform(parent);
        for (int i = 0; i < jsonMeshes.length(); i++) {
            JSONObject obj = jsonMeshes.getJSONObject(i);
            ShaderProgram sp = shaders.get(obj.getInt("shader"));
            Mesh m = new Mesh(sp, obj, model);
            UniformTransform ut = new UniformTransform(JSONData.parseMat(obj.getString("transform")), t);
            m.getUniforms().addStruct(ut);
            m.getUniforms().setUniformBuffer("lightBlock", "lightBlock");
            meshes.add(m);
        }
    }

    @Override
    public void initRender() {
        for (ShaderProgram sp : shaders) {
            sp.compileShader();
        }

        for (Mesh m : meshes) {
            glUseProgram(m.sp.getProgram());
            //create a new VAO for each renderer
            m.vaoHandle = GL30.glGenVertexArrays();
            glBindVertexArray(m.vaoHandle);
            m.vertexHandle = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, m.vertexHandle);
            int vertexBufferIndex = m.json.getInt("vertices");
            glBufferData(GL_ARRAY_BUFFER, model.getBuffer(vertexBufferIndex), GL_STATIC_DRAW);

            JSONArray attributes = m.json.getJSONArray("attributes");
            int vertexSize = m.json.getInt("vertex_size");

            for (int i = 0; i < attributes.length(); i++) {
                JSONObject attr = attributes.getJSONObject(i);
                m.attributeLocations.add(GL20.glGetAttribLocation(m.sp.getProgram(), attr.getString("name")));
                glEnableVertexAttribArray(m.attributeLocations.get(i));
                glVertexAttribPointer(m.attributeLocations.get(i), attr.getInt("number"),
                        RenderManager.parseGLType(attr.getString("type")), false, vertexSize, attr.getInt("offset"));
            }
            m.numberFaces = m.json.getInt("face_num");
            m.faceHandle = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, m.faceHandle);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, model.getBuffer(m.json.getInt("faces")), GL_STATIC_DRAW);

            if (m.json.has("material")) {
                String materialName = m.json.getString("material");
                m.mat = materials.get(materialName);
                m.uniforms.addMaterialUniforms(m.mat);
            }
        }

    }

    @Override
    public void render() {
        for (Mesh m : meshes) {
            glBindVertexArray(m.vaoHandle);
            m.sp.setUniformData(m.uniforms);
            RenderManager.getInstance().useShaderProgram(m.sp);
            //for now only tris are considered
            GL11.glDrawElements(GL_TRIANGLES, m.numberFaces * 3, GL_UNSIGNED_INT, 0);
        }
    }

    public List<ShaderProgram> getShaderPrograms() {
        return shaders;
    }

    public List<Mesh> getMeshes() {
        return meshes;
    }

    public static JSONRenderer createJSONRenderer(Component parent, String path) {
        JSONRenderer r = new JSONRenderer(parent, ResourceManager.getInstance().loadResource(path, new JSONData()).getData());
        RenderManager.getInstance().add(r);
        return r;
    }

    /* Mesh represents a single visual element
     * Each mesh has a shader and material that may be shared with others
     * Each mesh is rendered individually for now though
     * Meshes also maintain access to their JSON to enable parsing of custom properties
     */
    public class Mesh {

        ShaderProgram sp;
        UniformData uniforms;
        int vaoHandle;
        int vertexHandle;
        int faceHandle;
        List<Integer> attributeLocations;
        JSONObject json;
        JSONData parent;
        int numberFaces;
        Material mat;

        Mesh(ShaderProgram sp, JSONObject json, JSONData parent) {
            this.sp = sp;
            this.uniforms = new UniformData(sp);
            this.json = json;
            attributeLocations = new ArrayList<>();
            this.parent = parent;
        }

        public UniformData getUniforms() {
            return uniforms;
        }

        public JSONObject getJSON() {
            return json;
        }
    }

}
