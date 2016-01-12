package resource.collada;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.json.JSONArray;
import org.json.JSONObject;
import resource.Data;
import resource.JSONData;
import resource.Resource;
import resource.ResourceManager;

/**
 *
 * @author Andrew_2
 * 
 * loads Collada dae files with capability to write to custom JSON format
 * 
 * does not support animation yet
 */
public class ColladaModel extends Data {

    Map<String, MaterialWrapper> materials; // key = material.getName()
    Map<String, List<PolyList>> geometries; // key is geometry name, list is of all polylists in that geometry
    List<PolyList> visualMeshes; //the list of polylists in the visualScene
    String directory;

    public ColladaModel() {
    }

    @Override
    public void load(String path) {
        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(ResourceManager.getInstance().getFile(path));
            document.getDocumentElement().normalize();
            Element collada = (Element) document.getElementsByTagName("COLLADA").item(0);

            directory = ResourceManager.getDirectory(path);

            materials = processMaterials(collada, directory);
            geometries = processGeometries(collada);
            visualMeshes = processVisualScene(collada, materials, geometries);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void write(String outPath) {

        //instantiate new JSONData
        JSONData modelData = new JSONData(new JSONObject(), new ArrayList<>());
        JSONObject model = modelData.getJSON();

        //add header info
        model.put("version", "collada");
        model.put("date", LocalDateTime.now().toString());
        model.put("name", outPath);

        //write polylists
        for (PolyList p : visualMeshes) {
            p.writeJSON(modelData);
        }

        //write materials
        JSONObject materialJSON = new JSONObject();
        for (MaterialWrapper m : materials.values()) {
            if(m != null) {
                materialJSON.put(m.material.getName(), m.material.toJSON());
            }
        }
        model.put("materials", materialJSON);
        
        JSONArray shaders = new JSONArray();
        shaders.put(new JSONObject().put("vertex_shader", "texture.vs").put("fragment_shader", "texture.fs"));
        model.put("shaders", shaders);
        
        //write to file
        modelData.write(outPath);
    }

    public static void convertAndExport(String inPath, String outPath) {
        Resource<ColladaModel> m = ResourceManager.getInstance().loadResource(inPath, new ColladaModel());
        m.getData().write(outPath);
    }

    //process library_materials and library_images and library_effects
    private static Map<String, MaterialWrapper> processMaterials(Element collada, String directory) {
        //process images
        NodeList imagesXML = ((Element) collada.getElementsByTagName("library_images").item(0)).getElementsByTagName("image");
        Map<String, String> imageMap = new HashMap<>();
        for (int i = 0; i < imagesXML.getLength(); i++) {
            Element image = (Element) imagesXML.item(i);
            imageMap.put("#" + image.getAttribute("id"), directory + ((Element) image.getElementsByTagName("init_from").item(0)).getTextContent());
        }

        //process effects
        NodeList effectsXML = ((Element) collada.getElementsByTagName("library_effects").item(0)).getElementsByTagName("effect");
        Map<String, MaterialWrapper> effectMap = new HashMap<>();
        for (int i = 0; i < effectsXML.getLength(); i++) {
            Element effect = (Element) effectsXML.item(i);
            MaterialWrapper m = MaterialWrapper.parseMaterial(effect, imageMap);
            effectMap.put(m.effectName, m);
        }

        //process materials using the above images and effects
        Map<String, MaterialWrapper> materials = new HashMap<>();
        NodeList matXML = ((Element) collada.getElementsByTagName("library_materials").item(0)).getElementsByTagName("material");
        for (int i = 0; i < matXML.getLength(); i++) {
            Element matElem = (Element) matXML.item(i);
            //the material name as referenced elsewhere in the collada file
            String matName = "#" + matElem.getAttribute("id");
            //the name of the effect is only referenced from this material
            String effectName = ((Element) matElem.getElementsByTagName("instance_effect").item(0)).getAttribute("url");
            MaterialWrapper effect = effectMap.get(effectName);
            materials.put(matName, effect);
            effect.material.setName(matName);
        }

        return materials;
    }

    //process library_geometries
    private static Map<String, List<PolyList>> processGeometries(Element collada) {

        //a map from the geometry name to a list of polylist children
        Map<String, List<PolyList>> geometries = new HashMap<>();

        NodeList geomXML = ((Element) collada.getElementsByTagName("library_geometries").item(0)).getElementsByTagName("geometry");
        for (int i = 0; i < geomXML.getLength(); i++) {

            Element mesh = (Element) ((Element) geomXML.item(i)).getElementsByTagName("mesh").item(0);
            if (mesh == null) {
                continue;
            }
            //the geometry name as referenced elsewhere in the file
            String meshName = "#" + ((Element) geomXML.item(i)).getAttribute("id");

            //load all sources for this geometry
            Map<String, Source> sources = Source.processSources(mesh);

            //add extra reference to vertex data
            Element verticesXML = (Element) ((Element) mesh.getElementsByTagName("vertices").item(0));
            String vertName = "#" + verticesXML.getAttribute("id");
            sources.put(vertName, sources.get(((Element) verticesXML.getElementsByTagName("input").item(0)).getAttribute("source")));

            //process the polylists
            NodeList polygonListsXML = mesh.getElementsByTagName("polylist");
            List<PolyList> polyList = new ArrayList<>();
            for (int l = 0; l < polygonListsXML.getLength(); l++) {
                //polylist name formed from geometry name and index of polylist
                PolyList p = new PolyList((Element) polygonListsXML.item(l), sources, meshName + "_" + l);
                polyList.add(p);
            }
            geometries.put(meshName, polyList);
        }
        return geometries;
    }

    //parse array of floats from space delimited string
    static float[] parseFloatArray(String data) {
        if (data.isEmpty()) {
            return null;
        }
        String[] strings = data.split(" ");
        float[] floats = new float[strings.length];
        for (int i = 0; i < strings.length; i++) {
            floats[i] = Float.parseFloat(strings[i]);
        }
        return floats;
    }

    //parse array of ints from space delimited string
    static int[] parseIntArray(String data) {
        if (data.isEmpty()) {
            return null;
        }
        String[] strings = data.split(" ");
        int[] ints = new int[strings.length];
        for (int i = 0; i < strings.length; i++) {
            ints[i] = Integer.parseInt(strings[i]);
        }
        return ints;
    }

    //parse array of bools from space delimited string
    static boolean[] parseBoolArray(String data) {
        if (data.isEmpty()) {
            return null;
        }
        String[] strings = data.split(" ");
        boolean[] bools = new boolean[strings.length];
        for (int i = 0; i < strings.length; i++) {
            bools[i] = Boolean.parseBoolean(strings[i]);
        }
        return bools;
    }

    //process library_visual_scenes
    private static List<PolyList> processVisualScene(Element collada, Map<String, MaterialWrapper> materials, Map<String, List<PolyList>> geometries) {
        Element sceneXML = (Element) ((Element) collada.getElementsByTagName("library_visual_scenes").item(0)).getElementsByTagName("visual_scene").item(0);
        NodeList nodes = sceneXML.getElementsByTagName("node");
        List<PolyList> visualMeshes = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            Element geometryXML = (Element) node.getElementsByTagName("instance_geometry").item(0);
            //ignore nodes without geometry for now
            if(geometryXML == null) {
                continue;
            }
            String geometryName = geometryXML.getAttribute("url");
            List<PolyList> geom = geometries.get(geometryName);
            Element bindMaterial = (Element) geometryXML.getElementsByTagName("bind_material").item(0);
            if (bindMaterial != null) {
                Element materialXML = (Element) ((Element) bindMaterial.getElementsByTagName("technique_common").item(0)).getElementsByTagName("instance_material").item(0);
                //the name of the material to be bound will be read symbol for now
                String materialName = materialXML.getAttribute("symbol");
                MaterialWrapper wrapper = materials.get(materialName);
                NodeList binds = materialXML.getElementsByTagName("bind_vertex_input");
                Map<Integer, String> setToSemantic = new HashMap<>();
                //for each vertex_input create a map from the set number to the semantic referenced by the material
                for (int j = 0; j < binds.getLength(); j++) {
                    Element bind = (Element) binds.item(j);
                    String semanticTexCoord = bind.getAttribute("semantic");
                    int attrSet = Integer.parseInt(bind.getAttribute("input_set"));
                    setToSemantic.put(attrSet, semanticTexCoord);
                }
                for (PolyList pg : geom) {
                    for (List<Attribute> aList : pg.attributes) {
                        for (Attribute a : aList) {
                            if (a.set != -1) {
                                //change the semantic from TEX_COORD to the semantic specified by the bind material
                                //the bind material semantic is the name of the uv map as exported by blender
                                String semanticName = setToSemantic.get(a.set);
                                a.semantic = semanticName;
                            }
                        }
                    }
                }

            }

            //add the initial transformation of the geometry to the polylists in the geometry
            String transform = parseTransform(node);

            for (PolyList pg : geom) {
                visualMeshes.add(pg);
                pg.transform = transform;
            }

        }
        return visualMeshes;
    }

    //returns the transform string from a node element in visual scene
    public static String parseTransform(Element node) {
        NodeList mats = node.getElementsByTagName("matrix");
        for (int i = 0; i < mats.getLength(); i++) {
            Element matXML = (Element) mats.item(i);
            if (matXML.getAttribute("sid").equals("transform")) {

                String data = matXML.getTextContent();
                return data;
            }
        }
        return null;

    }

}

enum DataType {

    FLOAT(Float.BYTES, "f"), INTEGER(Integer.BYTES, "i"), BOOLEAN(Integer.BYTES, "b");

    private final int byteSize;
    private final String string;

    private DataType(int size, String string) {
        this.byteSize = size;
        this.string = string;
    }

    public int getByteSize() {
        return byteSize;
    }

    @Override
    public String toString() {
        return string;
    }
}
