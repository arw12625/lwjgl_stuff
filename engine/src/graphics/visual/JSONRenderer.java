package graphics.visual;

import game.Component;
import game.StandardGame;
import geometry.Transform;
import graphics.RenderLayer;
import graphics.RenderManager;
import graphics.util.RenderableAdapter;
import graphics.ShaderProgram;
import graphics.UniformBuffer;
import graphics.UniformData;
import graphics.util.UniformTransform;
import graphics.VAO;
import graphics.View;
import graphics.util.Camera;
import graphics.util.GraphicsUtility;
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
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import resource.JSONData;
import resource.ResourceManager;
import resource.TextureData;

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
public class JSONRenderer extends RenderableAdapter {

    private JSONData model;
    private Map<String, Material> materials;
    private List<Mesh> meshes;
    private List<ShaderProgram> shaders;
    private UniformBuffer lighting;

    public JSONRenderer(JSONData model,
            RenderManager renderManager, ResourceManager resourceManager,
            UniformBuffer lighting) {
        this.lighting =lighting;
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
                TextureData.loadTextureResource(value, renderManager, resourceManager);
            }
            materials.put(matName, mat);
        }

        JSONArray jsonShaders = model.getJSON().getJSONArray("shaders");
        for (int i = 0; i < jsonShaders.length(); i++) {
            shaders.add(ShaderProgram.loadProgram("shaders/" + jsonShaders.getJSONObject(i).getString("vertex_shader"),
                    "shaders/" + jsonShaders.getJSONObject(i).getString("fragment_shader"), renderManager, resourceManager));
        }
        JSONArray jsonMeshes = model.getJSON().getJSONArray("meshes");
        
        Transform t = new Transform();
        for (int i = 0; i < jsonMeshes.length(); i++) {
            JSONObject obj = jsonMeshes.getJSONObject(i);
            ShaderProgram sp = shaders.get(obj.getInt("shader"));
            Mesh m = new Mesh(sp, obj, model);
            UniformTransform ut = new UniformTransform(t, JSONData.parseMat(obj.getString("transform")));
            m.ut = ut;
            m.getUniforms().addStruct(ut);
            meshes.add(m);
        }
        
    }

    @Override
    public void renderInit() {
        
        for (ShaderProgram sp : shaders) {
            sp.createAndCompileShader();
        }

        for (Mesh m : meshes) {
            m.sp.useShaderProgram();
            
            //create a new VAO for each renderer
            m.vao.generateVAO();
            m.vao.useAndUpdateVAO();
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
        
        setRenderInitialized();

    }

    @Override
    public void render(View view, RenderLayer layer) {
        Camera c = GraphicsUtility.getHackyCamera(view);
        
        for (Mesh m : meshes) {
            m.vao.useAndUpdateVAO();
            m.ut.setCamera(c);
            m.getUniforms().setUniformBuffer("lightBlock", lighting.getGLBuffer(view));
            
            m.sp.setUniformData(m.uniforms);
            m.sp.useShaderProgram();
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

    public static JSONRenderer createJSONRenderer(String path, StandardGame game, UniformBuffer lighting) {
        return createJSONRenderer(path, game.getRenderManager(), game.getResourceManager(), lighting);
    }
    
    
    public static JSONRenderer createJSONRenderer(String path,
            RenderManager renderManager, ResourceManager resourceManager,
            UniformBuffer lighting) {
        JSONData jsonData = JSONData.loadJSONData(path, resourceManager);
        JSONRenderer r = new JSONRenderer(jsonData,
        renderManager, resourceManager, lighting);
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
        VAO vao;
        int vertexHandle;
        int faceHandle;
        List<Integer> attributeLocations;
        JSONObject json;
        JSONData parent;
        int numberFaces;
        Material mat;
        UniformTransform ut;

        Mesh(ShaderProgram sp, JSONObject json, JSONData parent) {
            this.sp = sp;
            this.uniforms = new UniformData(sp);
            this.json = json;
            attributeLocations = new ArrayList<>();
            this.parent = parent;
            vao = new VAO(sp.getRenderManager());
        }

        public UniformData getUniforms() {
            return uniforms;
        }

        public JSONObject getJSON() {
            return json;
        }
    }

}
