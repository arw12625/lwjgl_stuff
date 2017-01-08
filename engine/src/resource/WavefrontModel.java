package resource;

import graphics.visual.Material;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andy
 * 
 * loads a wavefront .obj file and associated material .mtl files
 * also contains code for converting model to custom json format
 */
public class WavefrontModel implements Data {

    private List<Vector3f> vertices;
    private List<Vector3f> normals;
    private List<Vector2f> texCoords;
    private List<Material> materialList;
    private List<WavefrontMesh> objects;
    
    private static final Logger LOG = LoggerFactory.getLogger(WavefrontModel.class);
            
    public WavefrontModel() {
    }

    public List<Vector3f> getVertices() {
        return vertices;
    }

    public List<Vector3f> getNormals() {
        return normals;
    }

    public List<Vector2f> getTexCoords() {
        return texCoords;
    }

    public List<Material> getMaterials() {
        return materialList;
    }

    @Override
    public void load(String path, ResourceManager resourceManager) {
        vertices = new ArrayList<>();
        normals = new ArrayList<>();
        texCoords = new ArrayList<>();
        materialList = new ArrayList<>();
        objects = new ArrayList<>();

        materialList.add(Material.defaultMaterial);
        String directory = ResourceManager.getDirectory(path);

        try {
            BufferedReader reader = resourceManager.getReader(path);
            HashMap<String, Integer> materialMap = new HashMap<>();
            String line;
            int currentMaterial = -1;
            WavefrontMesh currentMesh = null;

            //Read in objects
            while ((line = reader.readLine()) != null) {
                //Ignnore lines with #
                if (line.startsWith("#")) {
                    continue;
                }
                String[] spaceSplit = line.split(" ");
                //Read material library
                if (line.startsWith("mtllib ")) {
                    String materialFileName = spaceSplit[1];
                    Map<String, Material> libMats = loadMaterialLibrary(directory, materialFileName, resourceManager);
                    for (String matName : libMats.keySet()) {
                        materialMap.put(matName, materialList.size());
                        materialList.add(libMats.get(matName));
                    }
                } //A new object is read
                else if (line.startsWith("o ")) {
                    currentMesh = new WavefrontMesh();
                    currentMesh.name = spaceSplit[1];
                    objects.add(currentMesh);
                } //Set material for mesh
                else if (line.startsWith("usemtl ")) {
                    currentMaterial = materialMap.getOrDefault(spaceSplit[1], 0);
                } //Read in a vertex
                else if (line.startsWith("v ")) {
                    String[] xyz = spaceSplit;
                    float x = Float.valueOf(xyz[1]);
                    float y = Float.valueOf(xyz[2]);
                    float z = Float.valueOf(xyz[3]);
                    vertices.add(new Vector3f(x, y, z));
                } //Read in a normal vector
                else if (line.startsWith("vn ")) {
                    String[] xyz = spaceSplit;
                    float x = Float.valueOf(xyz[1]);
                    float y = Float.valueOf(xyz[2]);
                    float z = Float.valueOf(xyz[3]);
                    normals.add(new Vector3f(x, y, z));
                } //Read in a texture coord
                else if (line.startsWith("vt ")) {
                    String[] xyz = spaceSplit;
                    float x = Float.valueOf(xyz[1]);
                    float y = Float.valueOf(xyz[2]);
                    texCoords.add(new Vector2f(x, y));
                } //Read in a new Face
                else if (line.startsWith("f ")) {
                    String[] faceIndices = spaceSplit;
                    WavefrontFace face = new WavefrontFace(faceIndices.length - 1);
                    face.materialIndex = currentMaterial;
                    for (int i = 0; i < 3; i++) {
                        String[] vertexInfo = faceIndices[i + 1].split("/");
                        face.vecIndex[0][i] = Integer.parseInt(vertexInfo[0]) - 1;
                        if (vertexInfo.length > 1) {
                            if (!vertexInfo[1].isEmpty()) {
                                face.vecIndex[1][i] = Integer.parseInt(vertexInfo[1]) - 1;
                            }
                        }
                        if (vertexInfo.length > 2) {
                            face.vecIndex[2][i] = Integer.parseInt(vertexInfo[2]) - 1;
                        }
                    }
                    currentMesh.faces.add(face);
                }
            }

            reader.close();

        } catch (IOException e) {
            LOG.error("{}",e);
        }
    }
    
    @Override
    public void write(String path, ResourceManager resourceManager) {
        throw new UnsupportedOperationException("Writing Wavefront models isn't supported yet");
    }

    public static Map<String, Material> loadMaterialLibrary(String pathPrefix, String materialFilePath, ResourceManager resourceManager) {
        HashMap<String, Material> materials = new HashMap<>();
        try {
            BufferedReader reader = resourceManager.getReader(pathPrefix + materialFilePath);
            String line;
            Material parseMaterial = null;
            String materialName = "";
            while ((line = reader.readLine()) != null) {
                //ignore lines with #
                if (line.startsWith("#")) {
                    continue;
                }
                String[] spaceSplit = line.split(" ");
                //Load new Material
                if (line.startsWith("newmtl ")) {
                    if (!materialName.equals("")) {
                        materials.put(materialName, parseMaterial);
                    }
                    if (spaceSplit.length == 1) {
                        materialName = "";
                    } else {
                        materialName = spaceSplit[1];
                    }
                    parseMaterial = new Material(materialName);
                } //Load Specular Coefficient
                else if (line.startsWith("Ns ")) {
                    parseMaterial.addColor("specular_coefficient", new float[] {Float.valueOf(spaceSplit[1])});
                } //Load Ambient Color
                else if (line.startsWith("Ka ")) {
                    parseMaterial.addColor("ambient",new float[]{Float.valueOf(spaceSplit[1]), Float.valueOf(spaceSplit[2]), Float.valueOf(spaceSplit[3])});
                } //Load Specular Color
                else if (line.startsWith("Ks ")) {
                    parseMaterial.addColor("specular",new float[]{Float.valueOf(spaceSplit[1]), Float.valueOf(spaceSplit[2]), Float.valueOf(spaceSplit[3])});
                } //Load Diffuse Color
                else if (line.startsWith("Kd ")) {
                    parseMaterial.addColor("diffuse",new float[]{Float.valueOf(spaceSplit[1]), Float.valueOf(spaceSplit[2]), Float.valueOf(spaceSplit[3])});
                } //Load Texture Map
                else if (line.startsWith("map_Kd")) {
                    parseMaterial.addTexture("diffuse", pathPrefix + spaceSplit[1]);
                } //Unknown line
                else {
                    //util.DebugMessages.getInstance().write("[MTL] Unknown Line: " + line);
                }
            }
            materials.put(materialName, parseMaterial);
            reader.close();
        } catch (IOException ex) {
            LOG.error("{}",ex);
        }
        return materials;
    }

    public static Map<String, Material> loadMaterialLibrary(String materialFilePath, ResourceManager resourceManager) {
        return loadMaterialLibrary("", materialFilePath, resourceManager);
    }

    @Override
    public String toString() {
        if (objects == null) {
            return super.toString();
        } else {
            return super.toString()
                    + "\nObjects: " + objects.size()
                    + "\nVertices: " + vertices.size()
                    + "\nNormals: " + normals.size()
                    + "\nTexCoords: " + texCoords.size()
                    + "\nMaterials: " + materialList.size();
        }
    }

    public boolean isTextured() {
        return !texCoords.isEmpty();
    }

    public static void convertAndExportModel(String name, String pathIn, String pathOut, ResourceManager resourceManager) {
        WavefrontModel model = resourceManager.loadResource(pathIn, new WavefrontModel()).getData();
        JSONData data = convertModel(model);
        data.write(pathOut, resourceManager);
    }

    public static JSONData convertModel(WavefrontModel model) {
        return convertModel("untitled", model, null, null, null, null, null);
    }

    public static JSONData convertModel(String name, WavefrontModel model, boolean texturedAsAttr,
            boolean normalsAsAttr, boolean ambAsAttr, boolean diffAsAttr, boolean specAsAttr) {

        Map<String, Boolean> tex = new HashMap<>(), norm = new HashMap<>(),
                amb = new HashMap<>(), diff = new HashMap<>(), spec = new HashMap<>();
        for (WavefrontMesh mesh : model.objects) {
            tex.put(mesh.name, texturedAsAttr);
            norm.put(mesh.name, normalsAsAttr);
            amb.put(mesh.name, ambAsAttr);
            diff.put(mesh.name, diffAsAttr);
            spec.put(mesh.name, specAsAttr);
        }
        return convertModel(name, model, tex, norm, amb, diff, spec);
    }

    public static JSONData convertModel(String name, WavefrontModel model, Map<String, Boolean> texturedAsAttr,
            Map<String, Boolean> normalsAsAttr, Map<String, Boolean> ambAsAttr,
            Map<String, Boolean> diffAsAttr, Map<String, Boolean> specAsAttr) {

        if (texturedAsAttr == null) {
            texturedAsAttr = new HashMap<>();
        }
        if (normalsAsAttr == null) {
            normalsAsAttr = new HashMap<>();
        }
        if (ambAsAttr == null) {
            ambAsAttr = new HashMap<>();
        }
        if (diffAsAttr == null) {
            diffAsAttr = new HashMap<>();
        }
        if (specAsAttr == null) {
            specAsAttr = new HashMap<>();
        }

        JSONObject json = new JSONObject();
        json.put("version", "wavefront");
        json.put("date", LocalDateTime.now().toString());
        json.put("name", name);
        json.put("shaders", new JSONArray());
        
        JSONObject materialsJSON = new JSONObject();
        json.put("materials", materialsJSON);

        List<ByteBuffer> buffers = new ArrayList<>();

        int bufferPosition = 0;

        for (WavefrontMesh mesh : model.objects) {

            int vertsPerFace = 3;

            boolean tex = texturedAsAttr.getOrDefault(mesh.name, model.isTextured());
            boolean norm = normalsAsAttr.getOrDefault(mesh.name, true);
            boolean amb = ambAsAttr.getOrDefault(mesh.name, false);
            boolean diff = diffAsAttr.getOrDefault(mesh.name, false);
            boolean spec = specAsAttr.getOrDefault(mesh.name, false);
            boolean useMat = amb || diff || spec;

            //for each vertex used in the mesh, there is a list of indices
            //these values index the below lists of vertex data
            List<List<Integer>> vertexDataIndices = new ArrayList<>();
            //a list for convinience of the indices of vertex positions used in the mesh
            List<Integer> usedVertexIndices = new ArrayList();

            List<Integer> vertexPositions = new ArrayList<>();
            List<Integer> vertexTexCoords = new ArrayList<>();
            List<Integer> vertexNormals = new ArrayList<>();
            List<Integer> vertexMaterial = new ArrayList<>();

            //A list of faces for the current mesh with indices refering to the new schema
            int[][] faceData = new int[mesh.faces.size()][vertsPerFace];

            int vertexCount = 0;

            for (int faceIndex = 0; faceIndex < faceData.length; faceIndex++) {
                WavefrontFace face = mesh.faces.get(faceIndex);
                for (int i = 0; i < vertsPerFace; i++) {

                    //the index of the data in the new list
                    int dataIndex = 0;
                    //the index of the position in this mesh
                    int posIndex = usedVertexIndices.indexOf(face.vecIndex[0][i]);
                    if (posIndex == -1) {
                        posIndex = usedVertexIndices.size();
                        usedVertexIndices.add(face.vecIndex[0][i]);
                        vertexDataIndices.add(new ArrayList<>());
                    }
                    //the list of existing vertex indices corresponding to the old position index
                    List<Integer> posIndices = vertexDataIndices.get(posIndex);

                    boolean found = false;
                    int index = 0;
                    //for each vertex in the list, check if the current vertex is equivalent
                    for (index = 0; index < posIndices.size() && !found; index++) {
                        dataIndex = posIndices.get(index);
                        found = (!norm || vertexNormals.get(dataIndex) == face.vecIndex[2][i])
                                && (!tex || vertexTexCoords.get(dataIndex) == face.vecIndex[1][i])
                                && (!useMat || vertexMaterial.get(dataIndex) == face.materialIndex);
                    }
                    if (!found) {
                        vertexCount++;
                        index = posIndices.size();
                        dataIndex = vertexTexCoords.size();
                        posIndices.add(dataIndex);
                        vertexPositions.add(face.vecIndex[0][i]);
                        vertexTexCoords.add(face.vecIndex[1][i]);
                        vertexNormals.add(face.vecIndex[2][i]);
                        vertexMaterial.add(face.materialIndex);
                    }

                    //create new face with position and vec index
                    faceData[faceIndex][i] = dataIndex;

                }
            }

            JSONObject jsonMesh = new JSONObject();
            json.append("meshes", jsonMesh);
            jsonMesh.put("mesh_name", mesh.name);

            int vertexSize = 3 * Float.BYTES;
            JSONObject positionAttr = new JSONObject();
            positionAttr.put("name", "vertex_position").put("type", "f").put("number", 3).put("offset", 0);
            jsonMesh.append("attributes", positionAttr);
            if (tex) {
                JSONObject texAttr = new JSONObject();
                texAttr.put("name", "vertex_tex_coord").put("type", "f").put("number", 2);
                jsonMesh.append("attributes", texAttr);
                texAttr.append("offset", vertexSize);
                vertexSize += 2 * Float.BYTES;
            }
            if (norm) {
                JSONObject normalAttr = new JSONObject();
                normalAttr.put("name", "vertex_normal").put("type", "f").put("number", 3);
                jsonMesh.append("attributes", normalAttr);
                normalAttr.append("offset", vertexSize);
                vertexSize += 3 * Float.BYTES;
            }
            if (amb) {
                JSONObject ambAttr = new JSONObject();
                ambAttr.put("name", "vertex_ambient").put("type", "f").put("number", 3);
                jsonMesh.append("attributes", ambAttr);
                ambAttr.append("offset", vertexSize);
                vertexSize += 3 * Float.BYTES;
            }
            if (diff) {
                JSONObject diffAttr = new JSONObject();
                diffAttr.put("name", "vertex_diffuse").put("type", "f").put("number", 3);
                jsonMesh.append("attributes", diffAttr);
                diffAttr.append("offset", vertexSize);
                vertexSize += 3 * Float.BYTES;
            }
            if (spec) {
                JSONObject specAttr = new JSONObject();
                specAttr.put("name", "vertex_specular").put("type", "f").put("number", 3);
                jsonMesh.append("attributes", specAttr);
                specAttr.append("offset", vertexSize);
                vertexSize += 3 * Float.BYTES;
            }
            int faceSize = vertsPerFace * Integer.BYTES;

            JSONObject jsonShader = new JSONObject();
            if (tex) {
                jsonShader.put("vertex_shader", "texture.vs").put("fragment_shader", "texture.fs");
            } else {
                jsonShader.put("vertex_shader", "pervertex.vs").put("fragment_shader", "passThrough.fs");
            }
            JSONArray shaders = json.getJSONArray("shaders");
            shaders.put(jsonShader);
            int shaderIndex = shaders.length() - 1;
            jsonMesh.put("shader", shaderIndex);

            int vertexBufferSize = vertexSize * vertexCount;
            ByteBuffer vertexBuffer = BufferUtils.createByteBuffer(vertexBufferSize);
            for (int i = 0; i < vertexPositions.size(); i++) {

                putVector3f(vertexBuffer, model.vertices.get(vertexPositions.get(i)));
                if (tex) {
                    putVector2f(vertexBuffer, model.texCoords.get(vertexTexCoords.get(i)));
                }
                if (norm) {
                    putVector3f(vertexBuffer, model.normals.get(vertexNormals.get(i)));
                }
                if (useMat) {
                    Material m = model.materialList.get(vertexMaterial.get(i));
                    jsonMesh.put("material", m.getName());
                    materialsJSON.put(m.getName(), m.toJSON());
                }
            }
            vertexBuffer.rewind();

            int faceBufferSize = faceSize * faceData.length;
            ByteBuffer faceBuffer = BufferUtils.createByteBuffer(faceBufferSize);
            for (int i = 0; i < faceData.length; i++) {
                for (int j = 0; j < vertsPerFace; j++) {
                    faceBuffer.putInt(faceData[i][j]);
                }
            }
            faceBuffer.rewind();

            JSONObject jsonVertexBuffer = new JSONObject();
            jsonVertexBuffer.put("name", mesh.name + "_vertices").put("length", vertexBufferSize).put("position", bufferPosition);
            bufferPosition += vertexBufferSize;
            json.append("buffers", jsonVertexBuffer);
            buffers.add(vertexBuffer);
            jsonMesh.put("vertices", buffers.size() - 1);
            JSONObject jsonFaceBuffer = new JSONObject();
            jsonFaceBuffer.put("name", mesh.name + "_faces").put("length", faceBufferSize).put("position", bufferPosition);
            bufferPosition += faceBufferSize;
            json.append("buffers", jsonFaceBuffer);
            buffers.add(faceBuffer);
            jsonMesh.put("faces", buffers.size() - 1);
            jsonMesh.put("face_num", mesh.faces.size());
            jsonMesh.put("face_type", "tris");
            jsonMesh.put("vertex_size", vertexSize);

        }

        return new JSONData(json, buffers);
    }

    public static void putVector3f(ByteBuffer b, Vector3f v) {
        b.putFloat(v.x).putFloat(v.y).putFloat(v.z);
    }

    public static void putVector2f(ByteBuffer b, Vector2f v) {
        b.putFloat(v.x).putFloat(v.y);
    }

    class WavefrontMesh {

        public String name;
        public List<WavefrontFace> faces;

        public WavefrontMesh() {
            faces = new ArrayList<>();
        }

    }

    class WavefrontFace {

        public int materialIndex;
        //3x3 array, first row is position, second texture, third normal
        public int[][] vecIndex;

        public WavefrontFace(int vertsPerFace) {
            vecIndex = new int[3][vertsPerFace];
        }

    }

    class FaceData {

        int[] dataIndex;

        public FaceData(int[] dataIndex) {
            this.dataIndex = dataIndex;
        }
    }

}
