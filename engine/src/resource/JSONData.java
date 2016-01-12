package resource;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.lwjgl.BufferUtils;

/**
 *
 * @author Andrew_2
 * 
 * JSONData represents general game data using a JSON file in conjuction with a binary dat file
 * 
 * The json file represents plain text game information with list of buffers
 * Binary data loaded into individual buffers according to the buffers list in the json
 * 
 */
public class JSONData extends Data {

    private String name;
    private List<ByteBuffer> buffers;
    private JSONObject object;
    private int bufferPosition;

    public JSONData() {
    }
    
    public JSONData(JSONObject object, List<ByteBuffer> buffers) {
        this.object = object;
        this.buffers = buffers;
    }

    @Override
    public void load(String path) {
        String text = TextData.loadText(path);
        JSONTokener tokener = new JSONTokener(text);
        JSONObject obj = new JSONObject(tokener);
        this.object = obj;
        this.name = obj.getString("name");
        this.buffers = new ArrayList<>();
        List<Integer> bufferLengths = new ArrayList<>();
        List<Integer> bufferPositions = new ArrayList<>();
        JSONArray buffersJSON = obj.getJSONArray("buffers");
        for (int i = 0; i < buffersJSON.length(); i++) {
            bufferLengths.add(buffersJSON.getJSONObject(i).getInt("length"));
            bufferPositions.add(buffersJSON.getJSONObject(i).getInt("position"));
        }

        String dataPath = obj.getString("data_path");
        try {
            FileInputStream fs = ResourceManager.getInstance().getFileInputStream(dataPath);
            FileChannel channel = fs.getChannel();

            for (int i = 0; i < bufferLengths.size(); i++) {
                ByteBuffer b = BufferUtils.createByteBuffer(bufferLengths.get(i));
                channel.read(b, bufferPositions.get(i));
                b.rewind();
                buffers.add(b);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String path) {

        try {
            String fileName = ResourceManager.getFileNameNoExt(path);
            String directory = ResourceManager.getDirectory(path);
            String dataPath = directory + fileName + ".dat";
                  
            object.put("data_path", dataPath);
            BufferedWriter writer = ResourceManager.getInstance().getWriter(path);
            writer.write(object.toString(3));
            writer.flush(); writer.close();
            
            FileOutputStream fs = ResourceManager.getInstance().getFileOutputStream(dataPath);
            FileChannel channel = fs.getChannel();
            JSONArray buffersJSON = object.getJSONArray("buffers");
            for(int i = 0; i < buffersJSON.length(); i++) {
                int pos = buffersJSON.getJSONObject(i).getInt("position");
                channel.write(buffers.get(i), pos);
                
                buffers.get(i).rewind();
            }
            channel.close();
            fs.flush(); fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public JSONObject getJSON() {
        return object;
    }

    public int appendToList(String name, Object o) {
        object.append(name, o);
        return object.getJSONArray(name).length() - 1;
    }
    
    public int addBuffer(String name, ByteBuffer b) {
        buffers.add(b);
        JSONObject bufferItem = new JSONObject();
        bufferItem.put("name", name);
        int size = b.remaining();
        bufferItem.put("length", size);
        bufferItem.put("position", bufferPosition);
        bufferPosition += size;
        return appendToList("buffers", bufferItem);
    }
    
    public ByteBuffer getBuffer(int i) {
        return buffers.get(i);
    }

    public JSONObject getJSONObject() {
        return object;
    }
    
    public String getJSONName() {
        return name;
    }

    @Override
    public String toString() {
        return getJSONName();
    }
    
    public static float[] jsonToFloatArray(JSONArray json) {
        if(json == null) {
            return null;
        }
        float[] array = new float[json.length()];
        for(int i = 0; i < array.length; i++) {
            array[i] = (float)json.getDouble(i);
        }
        return array;
    }
    public static Matrix4f parseMat(String matString) {
        if(matString.isEmpty()) {
            return new Matrix4f();
        }
        String[] strings = matString.split(" ");
        float[] array = new float[strings.length];
        for(int i = 0; i < strings.length; i++) {
            array[i] = Float.parseFloat(strings[i]);
        }
        Matrix4f mat = new Matrix4f();
        mat.set(array);
        mat.transpose();
        return mat;
    }
}
