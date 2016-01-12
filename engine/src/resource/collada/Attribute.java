package resource.collada;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Andrew_2
 * 
 * describes an attribute of a vertex of a mesh to be used in a vertex shader
 */
public class Attribute {

    int offset;             //the index of this attribute in the data
    int byteOffset;         //the byte index version of the offset
    String semantic;        //a name describing this attribute ex TEX_COORD, VERTEX, NORMAL
    String sourceName;      //the name of the source from which data is taken
    Source source;          //the corresponding source
    int set;                //the set describes allows a correspondence between materials and tex_coords

    public Attribute(int offset, String semantic, String sourceName, Source source, int set) {
        this.offset = offset;
        this.semantic = semantic;
        this.source = source;
        this.sourceName = sourceName;
        this.set = set;
    }

    public void writeJSON(JSONObject meshJSON) {
        JSONObject attrJSON = new JSONObject();
        attrJSON.put("number", source.getNumParams());
        String attrName = Attribute.processAttributeName(this);
        attrJSON.put("name", attrName);
        attrJSON.put("type", source.type.toString());
        attrJSON.put("offset", byteOffset);
        meshJSON.append("attributes", attrJSON);
    }

    public static int getVertexByteSize(List<List<Attribute>> attributes) {
        int size = 0;
        for (List<Attribute> a : attributes) {
            size += a.get(0).source.getByteSize();
        }
        return size;
    }

    public static List<List<Attribute>> processAttributes(Element polygonsXML, Map<String, Source> sourcesMap) {
        NodeList attributesXML = polygonsXML.getElementsByTagName("input");
        //the list is indexed by the offset of the Attribute
        List<List<Attribute>> attributes = new ArrayList<>();
        for (int i = 0; i < attributesXML.getLength(); i++) {
            Element attribXML = (Element) attributesXML.item(i);
            int offset = Integer.parseInt(attribXML.getAttribute("offset"));
            String sourceName = attribXML.getAttribute("source");
            String semantic = attribXML.getAttribute("semantic");
            Source source = sourcesMap.get(sourceName);
            int set = -1;
            //process set number
            String setString = attribXML.getAttribute("set");
            if(!setString.isEmpty()) {
                set = Integer.parseInt(setString);
            }
            //order list of attributes according to offset
            //add padding inbetween
            if(offset >= attributes.size()) {
                for(int j = attributes.size(); j <= offset; j++) {
                    attributes.add(new ArrayList<>());
                }
            }
            List<Attribute> offsetList = attributes.get(offset);
            offsetList.add(new Attribute(offset, semantic, sourceName, source, set));
        }
        int byteOffset = 0;
        for(int i = 0; i < attributes.size(); i++) {
            List<Attribute> aList = attributes.get(i);
            for(Attribute a : aList) {
                a.byteOffset = byteOffset;
            }
            byteOffset += aList.get(0).source.getByteSize();
        }
        return attributes;
    }

    //translate attribute name into name used in shader
    public static String processAttributeName(Attribute attr) {
        String name;
        switch (attr.semantic) {
            case "VERTEX":
                name = "vertex_position";
                break;
            case "POSITION":
                name = "vertex_position";
                break;
            case "NORMAL":
                name = "vertex_normal";
                break;
            default:
                name = attr.semantic;
        }
        return name;
    }

}
