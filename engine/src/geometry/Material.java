package geometry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import resource.JSONData;

/**
 *
 * @author Andy
 * 
 * Material properties of a mesh
 * For now, only visible properties are represented
 * Materials only contain a list of colors and textures
 * 
 */
public class Material {

    private String name;
    //the name of the color must be the name of the uniform it is bound to
    private Map<String, Color> colors;
    //the name of the texture must be the name of the sampler it is bound to
    private Map<String, String> textures;

    public static final Material defaultMaterial = new Material("default", new float[]{.3f, .3f, .3f});

    public Material() {
        this("");
    }

    public Material(String name) {
        this.name = name;
        colors = new HashMap<>();
        textures = new HashMap<>();
    }

    public Material(String name, float[] diffuse) {
        this(name);
        colors.put("diffuse", new Color(diffuse));
    }

    public void addColor(String name, float[] color) {
        addColor(name, new Color(color));
    }

    public void addColor(String name, Color color) {
        colors.put(name, color);
    }

    public Color getColor(String name) {
        return colors.get(name);
    }

    public float[] getColorData(String name) {
        return getColor(name).getData();
    }

    public void addTexture(String name, String textureName) {
        textures.put(name, textureName);
    }

    public String getTexture(String name) {
        return textures.get(name);
    }

    public Map<String, String> getTextureMap() {
        return textures;
    }

    public Map<String, Color> getColorMap() {
        return colors;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public JSONObject toJSON() {
        JSONObject matJSON = new JSONObject();
        matJSON.put("name", name);
        JSONObject colorsJSON = new JSONObject();
        for (Map.Entry<String, Color> entry : colors.entrySet()) {
            colorsJSON.put(entry.getKey(), entry.getValue().getRGBA());
        }
        //matJSON.put("colors", colorsJSON);

        JSONObject texturesJSON = new JSONObject();
        for (Map.Entry<String, String> entry : textures.entrySet()) {
            texturesJSON.put(entry.getKey(), entry.getValue());
        }
        matJSON.put("textures", texturesJSON);
        return matJSON;
    }

    public static Material fromJSON(JSONObject mat) {
        Material m = new Material(mat.getString("name"));
        JSONObject colorsJSON = mat.optJSONObject("colors");
        if (colorsJSON != null) {
            Set<String> colorNames = colorsJSON.keySet();
            for (String colorName : colorNames) {
                JSONArray colorJSON = colorsJSON.getJSONArray(colorName);
                m.addColor(colorName, JSONData.jsonToFloatArray(colorJSON));
            }
        }
        JSONObject texturesJSON = mat.optJSONObject("textures");
        if (texturesJSON != null) {
            Set<String> samplerNames = texturesJSON.keySet();
            for (String sampName : samplerNames) {
                String texName = texturesJSON.getString(sampName);
                m.addTexture(sampName, texName);
            }
        }
        return m;
    }

    //A wrapper class for float data that will allow different formats (rgb, argb)
    public class Color {

        private float[] data;

        public Color(float[] data) {
            this.data = data;
        }

        public int getNumComponents() {
            return data.length;
        }

        public float[] getData() {
            return data;
        }

        public float[] getRGB() {
            float[] rgb = new float[3];
            System.arraycopy(data, 0, rgb, 0, rgb.length);
            return rgb;
        }

        public float[] getRGBA() {
            float[] rgba = new float[4];
            System.arraycopy(data, 0, rgba, 0, 3);
            if (data.length > 3) {
                rgba[3] = data[3];
            } else {
                rgba[3] = 1;
            }
            return rgba;
        }

    }
}
