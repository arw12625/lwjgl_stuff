package resource.collada;

import geometry.Material;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Andrew_2
 * 
 * a wrapper for materials containing data from library_materials, library_effects, and library_images
 */
public class MaterialWrapper {

    String effectName;
    Material material;
    Map<String, List<String>> texcoordToTextures;

    public MaterialWrapper(Material m) {
        this.material = m;
        this.texcoordToTextures = new HashMap<>();
    }

    public void putTex(String texCoordName, String texture) {
        List<String> list = texcoordToTextures.get(texCoordName);
        if (list == null) {
            list = new ArrayList<>();
            texcoordToTextures.put(texCoordName, list);
        }
        list.add(texture);
    }

    static MaterialWrapper parseMaterial(Element effect, Map<String, String> imageMap) {
        String effectName = "#" + effect.getAttribute("id");
        Element profile = (Element) effect.getElementsByTagName("profile_COMMON").item(0);

        //textures reference samplers
        //samplers reference surfaces
        //surfaces reference images
        NodeList params = profile.getElementsByTagName("newparam");
        //a map from sampler name to surface name
        Map<String, String> samplersMap = new HashMap<>();
        //a map from surface name to image name
        Map<String, String> surfacesMap = new HashMap<>();
        for (int j = 0; j < params.getLength(); j++) {
            Element param = (Element) params.item(j);
            String paramName = param.getAttribute("sid");
            NodeList surfacesXML = param.getElementsByTagName("surface");
            if (surfacesXML.getLength() == 1) {
                Element surfInit = (Element) ((Element) surfacesXML.item(0)).getElementsByTagName("init_from").item(0);
                surfacesMap.put(paramName, surfInit.getTextContent());
            }
            NodeList samplersXML = param.getElementsByTagName("sampler2D");
            if (samplersXML.getLength() == 1) {
                Element sampSource = (Element) ((Element) samplersXML.item(0)).getElementsByTagName("source").item(0);
                samplersMap.put(paramName, sampSource.getTextContent());
            }
        }

        Material m = new Material();
        MaterialWrapper wrapper = new MaterialWrapper(m);
        wrapper.effectName = effectName;
        
        Element phong = (Element) ((Element) effect.getElementsByTagName("technique").item(0)).getElementsByTagName("phong").item(0);
        NodeList details = phong.getChildNodes();
        for (int j = 0; j < details.getLength(); j++) {
            if (details.item(j).getNodeType() == Node.ELEMENT_NODE) {
                Element node = (Element) details.item(j);
                String detailName = node.getTagName();
                Element color = (Element) node.getElementsByTagName("color").item(0);
                if (color != null) {
                    //for colors the key is just the detailName ex) diffuse
                    m.addColor(detailName, parseColor(color));
                }
                NodeList texturesXML = node.getElementsByTagName("texture");
                if (texturesXML.getLength() != 0) {
                    for (int k = 0; k < texturesXML.getLength(); k++) {
                        Element texture = (Element) texturesXML.item(k);
                        //for textures the key is the detailName + "_ " + semantic ex) diffuse_diff
                        String samplerName = texture.getAttribute("texture");
                        String surfaceName = samplersMap.get(samplerName);
                        String imageName = "#" + surfacesMap.get(surfaceName);
                        String textureName = imageMap.get(imageName);   //the name of the texture file
                        String texCoordName = texture.getAttribute("texcoord");
                        String newTexName = detailName + "_" + texCoordName;    //the name of the uniform sampler in the shader
                        m.addTexture(newTexName, textureName);
                        wrapper.putTex(texCoordName, newTexName);
                    }
                }
            }
        }

        return wrapper;
    }

    static float[] parseColor(Element colorXML) {
        String text = colorXML.getTextContent();
        float[] color = new float[4];
        String[] strings = text.split(" ");
        for (int i = 0; i < color.length; i++) {
            color[i] = Float.parseFloat(strings[i]);
        }
        return color;
    }

    
}

