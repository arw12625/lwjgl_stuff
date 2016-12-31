
package resource.collada;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import resource.JSONData;

/**
 *
 * @author Andrew_2
 * 
 * describes mesh geometry
 */
public class PolyList {

    String polyName;
    int faceCount;                      //the number of faces
    int vertsPerFace;                   //the nubmer of vertices per face ex) 3 for tris
    String materialName;                //the name of the material in the material library
    ByteBuffer vertexBuffer;            //the vertex data of all the attributes interleaved
    ByteBuffer faceBuffer;              //the indices of the vertices that form the faces
    List<List<Attribute>> attributes;   //the list of the list of attributes with the same offset (the same data)
    String transform;                   //the string version of the transform ex) a 4x4 matrix
    
    private static final Logger LOG = LoggerFactory.getLogger(PolyList.class);

    public PolyList(Element polygonsXML, Map<String, Source> sources, String polyName) {

        this.polyName = polyName;
        materialName = polygonsXML.getAttribute("material");

        //for utility a list of the sources corresponding to each offset
        attributes = Attribute.processAttributes(polygonsXML, sources);
        List<Source> sourceList = new ArrayList<Source>();
        for(int i = 0; i < attributes.size(); i++) {
            sourceList.add(attributes.get(i).get(0).source);
        }

        //verify the number of vertsPerFace
        String[] vcount = ((Element) polygonsXML.getElementsByTagName("vcount").item(0)).getTextContent().split(" ");
        vertsPerFace = Integer.parseInt(vcount[0]);
        if (vertsPerFace != 3) {
            LOG.error("no support for not triangulated meshes");
        }

        //order the face data into an 2d array with rows corresponding to vertices and columns to attributes
        //fill with the reduced indices from the sources
        faceCount = Integer.parseInt(polygonsXML.getAttribute("count"));
        int[][] faces = new int[faceCount * vertsPerFace][attributes.size()];
        String[] facesString = ((Element) polygonsXML.getElementsByTagName("p").item(0)).getTextContent().split(" ");
        for (int j = 0; j < faces.length; j++) {
            for (int k = 0; k < faces[0].length; k++) {
                faces[j][k] = sourceList.get(k).getIndex(Integer.parseInt(facesString[j * faces[0].length + k]));
            }
        }

        //change faces referincing attributes individually to indexed groups of attributes forming vertices
        //the indices of vertices in vertexIndices that form faces
        int[] faceIndices = new int[faces.length];
        //a map from the new vertex indices to the old as in rows of the faces array
        List<Integer> vertexIndices = new ArrayList<>();
        processFaces(faces, faceIndices, vertexIndices);

        //compute byte size of a single vert and allocate buffer
        int vertSize = Attribute.getVertexByteSize(attributes);
        vertexBuffer = BufferUtils.createByteBuffer(vertSize * vertexIndices.size());
        for (int j = 0; j < vertexIndices.size(); j++) {
            int[] vertData = faces[vertexIndices.get(j)];
            for (int k = 0; k < attributes.size(); k++) {
                vertexBuffer.put(sourceList.get(k).getDataFromNewIndex(vertData[k]));
            }
        }
        vertexBuffer.rewind();

        int faceIndexSize = Integer.BYTES;
        faceBuffer = BufferUtils.createByteBuffer(faceIndexSize * faceIndices.length);
        faceBuffer.asIntBuffer().put(faceIndices);
        faceBuffer.rewind();

    }

    private static void processFaces(int[][] faceData, int[] faceIndices, List<Integer> vertexIndices) {

        for (int faceIndex = 0; faceIndex < faceData.length; faceIndex++) {
            //the current face vertex data indices
            int[] face = faceData[faceIndex];

            boolean found = false;
            int foundIndex = 0;
            //for each vertex in the list, check if the current vertex is equivalent
            for (int i = 0; i < vertexIndices.size() && !found; i++) {
                int index = vertexIndices.get(i);
                int[] checkFace = faceData[index];
                found = Arrays.equals(face, checkFace);
                foundIndex = i;
            }
            //if the vertex is not a duplicate, add to list
            if (!found) {
                foundIndex = vertexIndices.size();
                vertexIndices.add(faceIndex);
            }

            //create new face with position and vec index
            faceIndices[faceIndex] = foundIndex;

        }

    }
    
    //write mesh data to json with buffers for binary data
    public void writeJSON(JSONData modelJSONData) {
        JSONObject json = modelJSONData.getJSON();
        JSONObject meshJSON = new JSONObject();
        meshJSON.put("mesh_name", polyName);
        meshJSON.put("material", "#" + materialName);
        meshJSON.put("face_num", faceCount);
        meshJSON.put("face_type", vertsPerFace == 3 ? "tris" : "quads");
        meshJSON.put("vertex_size", Attribute.getVertexByteSize(attributes));
        for (int j = 0; j < attributes.size(); j++) {
            for(Attribute a : attributes.get(j)) {
                a.writeJSON(meshJSON);
            }
        }
        meshJSON.put("vertices", modelJSONData.addBuffer(polyName + "_verts_", vertexBuffer));
        meshJSON.put("faces", modelJSONData.addBuffer(polyName + "_faces_", faceBuffer));
        meshJSON.put("shader", 0);
        //todo choose shaders to export
        /*JSONObject shaderJSON = new JSONObject();
         if (textured) {
         shaderJSON.put("fragment_shader", "texture.fs");
         shaderJSON.put("vertex_shader", "texture.vs");
         } else {
         shaderJSON.put("fragment_shader", "passThrough.fs");
         shaderJSON.put("vertex_shader", "pervertex.vs");
         }
         int shaderIndex = modelJSONData.appendToList("shaders", shaderJSON);
         meshJSON.put("shader", shaderIndex);*/

        meshJSON.put("transform", transform);
        json.append("meshes", meshJSON);
    }

}