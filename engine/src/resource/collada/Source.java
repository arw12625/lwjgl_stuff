package resource.collada;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.BufferUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Andrew_2
 * 
 * describe data from collada from <source>
 */
public class Source {

    String name;
    DataType type;
    private ByteBuffer data;                //reduced binary version of the source data
    private List<Integer> indexMap;         //a map from original source indices to reduced data
    int count;
    int stride;                             //the number of entries grouped together for reading
    String[] paramNames;
    
    public static final int precision = 16; //precision for rounding, power of two

    public Source(Element xmlSource) {
        Element dataXML;
        
        name = "#" + xmlSource.getAttribute("id");

        Element accessor = (Element) ((Element) xmlSource.getElementsByTagName("technique_common").item(0)).getElementsByTagName("accessor").item(0);
        count = Integer.parseInt(accessor.getAttribute("count"));
        stride = Integer.parseInt(accessor.getAttribute("stride"));
        NodeList params = accessor.getElementsByTagName("param");
        paramNames = new String[params.getLength()];
        for (int i = 0; i < paramNames.length; i++) {
            paramNames[i] = ((Element) params.item(i)).getAttribute("name");
        }
        
        indexMap = new ArrayList<>();
        if ((dataXML = (Element) xmlSource.getElementsByTagName("float_array").item(0)) != null) {
            type = DataType.FLOAT;
            float[] floatData = ColladaModel.parseFloatArray(dataXML.getTextContent());
            data = Source.reduceFloatData(floatData, indexMap, count, stride, precision);
        } else if ((dataXML = (Element) xmlSource.getElementsByTagName("int_array").item(0)) != null) {
            type = DataType.INTEGER;
            int[] intData = ColladaModel.parseIntArray(dataXML.getTextContent());
            data = Source.reduceIntData(intData, indexMap, count, stride);
        } else if ((dataXML = (Element) xmlSource.getElementsByTagName("bool_array").item(0)) != null) {
            type = DataType.BOOLEAN;
            boolean[] boolData = ColladaModel.parseBoolArray(dataXML.getTextContent());
            data = Source.reduceBoolData(boolData, indexMap, count, stride);
        } else {
            data = null;
        }

    }

    public int getIndex(int index) {
        return indexMap.get(index);
    }
    
    //relative to original index
    public ByteBuffer getData(int index) {
        return getDataFromNewIndex(getIndex(index));
    }
    
    //relative to reduced index
    public ByteBuffer getDataFromNewIndex(int index) {
        data.clear();
        int byteOffset = getByteSize() * index;
        data.position(byteOffset);
        data.limit(data.position() + getByteSize());
        return data;
        /*
            //alternatively
            int byteOffset = getByteSize() * index;
            data.position(byteOffset);
            data.limit(data.position() + getByteSize());
            ByteBuffer slice = data.slice();
            data.clear();
            return slice;
        */
    }
    
    public int getNumParams() {
        return paramNames.length;
    }

    public int getByteSize() {
        return type.getByteSize() * stride;
    }

    public static Map<String, Source> processSources(Element container) {
        Map<String, Source> sources = new HashMap<>();
        NodeList sourcesXML = container.getElementsByTagName("source");
        for (int j = 0; j < sourcesXML.getLength(); j++) {
            Source s = new Source((Element) sourcesXML.item(j));
            sources.put(s.name, s);
        }
        return sources;
    }

    //reduce precision of floats and then remove duplicates in groups of size stride
    //and create mapping from original indices to new indices
    //returns binary reduced data
    private static ByteBuffer reduceFloatData(float[] floatData, List<Integer> indexMap, int count, int stride, int precision) {
        if(floatData == null) {
            return null;
        }
        //System.out.println("Before " + floatData.length / stride);
        
        //the reduced data in integers, maximum size is count
        int[][] reduced = new int[count][stride];
        //the current number of rows filled in reduced
        int reducedSize = 0;
        for(int i = 0; i < count; i++) {
            int startIndex = i * stride;
            int[] scaled = new int[stride];
            //reduce precision of current data by multiplying by power of two and truncating decimal
            for(int j = 0; j < stride; j++) {
                scaled[j] = (int)Math.scalb(floatData[startIndex + j], precision);
            }
            
            int dataIndex = 0;
            boolean found = false;
            //iterate through already reduced data to find if duplicate
            for(int j = 0; j < reducedSize && !found; j++) {
                dataIndex = j;
                found = Arrays.equals(scaled, reduced[dataIndex]);
            }
            //if not a duplicate then add data to reduced and set the appropriate index
            if(!found) {
                dataIndex = reducedSize;
                System.arraycopy(scaled, 0, reduced[dataIndex], 0, stride);
                reducedSize++;
            }
            //add entry in indexMap from the current index to the found index
            indexMap.add(dataIndex);
        }
        //create binary data from reduced
        ByteBuffer b = BufferUtils.createByteBuffer(reducedSize * stride * Float.BYTES);
        for(int i = 0; i < reducedSize; i++) {
            for(int j = 0; j < stride; j++) {
                //divide data by power of two and add to buffer
                b.putFloat(Math.scalb(reduced[i][j], -precision));
            }
        }
        b.rewind();
        //System.out.println("After " + reducedSize);
        return b;
    }

    //for now this just wraps the int data in a buffer
    private static ByteBuffer reduceIntData(int[] intData, List<Integer> indexMap, int count, int stride) {
        if(intData == null) {
            return null;
        }
        ByteBuffer b = BufferUtils.createByteBuffer(count * stride * Integer.BYTES);
        b.asIntBuffer().put(intData);
        b.rewind();
        for(int i = 0; i < count; i++) {
            indexMap.add(i);
        }
        return b;
    }

    //this creates an integer buffer where true -> 1 and false ->0
    private static ByteBuffer reduceBoolData(boolean[] boolData, List<Integer> indexMap, int count, int stride) {
        if(boolData == null) {
            return null;
        }
        ByteBuffer b = BufferUtils.createByteBuffer(count * stride * Integer.BYTES);
        for(int i = 0; i < count; i++) {
            for(int j = 0; j <stride; j++) {
                b.putInt(boolData[i * stride + j]?1:0);
            }
            indexMap.add(i);
        }
        b.rewind();
        return b;
    }
}
