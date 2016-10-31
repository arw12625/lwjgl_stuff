package graphics;

import geometry.Material;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
/**
 *
 * @author Andrew_2
 * 
 * An interface for setting the uniform variables within shaders
 * Each UniformData can only be used with one ShaderProgram (owner)
 * This is as the locations of variables depend upon the shader
 * Before use in setting uniforms, an instance must be bound to its shader
 * with 
 * 
 */
public class UniformData {

    private ShaderProgram owner;

    private List<Uniform> uniforms;
    private Map<String, Integer> uniformIndices;
    //uniforms that have not yet been created
    private Queue<Integer> uniformNew;
    //uniforms that have changed value since the last upload
    private Queue<Integer> uniformChanged;

    //the list of names of uniformBlocks used by this shader
    //uniform blocks must be added through the RenderManager as buffers
    private List<String> uniformBlocks;

    //the current set of texture units and the name of textures bound to them
    private Map<Integer, String> textureMap;
    
    private List<UniformStruct> structs;

    private int currentSize;
    private int bufferSize;
    //all uniform data is written into this buffer before uploading
    private ByteBuffer data;

    public UniformData(ShaderProgram sp) {
        this(sp, 1024);
    }

    public UniformData(ShaderProgram sp, int bufferSize) {
        this.owner = sp;

        uniforms = new ArrayList<>();
        uniformIndices = new HashMap<>();
        uniformNew = new ConcurrentLinkedQueue<>();
        uniformChanged = new ConcurrentLinkedQueue<>();

        uniformBlocks = new ArrayList<>();

        textureMap = new HashMap<>();
        
        structs = new ArrayList<>();
        
        currentSize = 0;
        data = BufferUtils.createByteBuffer(bufferSize);
    }

    /* createUniform must be called before setting a uniform
     * count refers to an array of uniforms, not a data type with multiple entries (ie vec3)
     * returns the internal index of the uniform, not the uniforms location in the shader
     * this index must be used to set the uniform data with the setUniform methods
     */
    public int createUniform(String name, GLType type, int count) {
        Integer index = uniformIndices.get(name);
        if (index == null) {
            index = uniforms.size();
            int offset = currentSize;
            Uniform uni = new Uniform(name, index, type, count, offset);
            currentSize += uni.getByteSize();
            uniformNew.add(index);
            uniformChanged.add(index);
            uniforms.add(uni);
            uniformIndices.put(name, index);
        }
        return index;
    }
    
    public int getUniform(String name) {
        return uniformIndices.get(name);
    }
    
    public void addStruct(UniformStruct str) {
        structs.add(str);
        str.createUniformStruct(this);
    }

    //mark all uniforms as changed
    protected void updateAll() {
        uniformChanged.clear();
        for(Uniform u : uniforms) {
            uniformChanged.add(u.index);
        }
    }
    
    //upload changed uniforms
    protected void updateUniforms() {
        for(int i = 0; i < structs.size(); i++) {
            structs.get(i).updateUniformStruct(this);
        }
        
        Integer index;
        while((index = uniformNew.poll()) != null) {
            Uniform uni = uniforms.get(index);
            uni.location = owner.getUniformLocation(uni.name);
        }

        while((index = uniformChanged.poll()) != null) {
            Uniform uni = uniforms.get(index);
            data.position(uni.offset);
            int loc = uni.location;
            int count = uni.count;
            switch (uni.type) {
                case GL_1fv:
                    GL20.glUniform1fv(loc, count, data);
                    break;
                case GL_1iv:
                    GL20.glUniform1iv(loc, count, data);
                    break;
                case GL_2fv:
                    GL20.glUniform2fv(loc, count, data);
                    break;
                case GL_2iv:
                    GL20.glUniform2iv(loc, count, data);
                    break;
                case GL_3fv:
                    GL20.glUniform3fv(loc, count, data);
                    break;
                case GL_3iv:
                    GL20.glUniform3iv(loc, count, data);
                    break;
                case GL_4fv:
                    GL20.glUniform4fv(loc, count, data);
                    break;
                case GL_4iv:
                    GL20.glUniform4iv(loc, count, data);
                    break;
                case GL_m2fv:
                    GL20.glUniformMatrix2fv(loc, count, false, data);
                    break;
                case GL_m3fv:
                    GL20.glUniformMatrix3fv(loc, count, false, data);
                    break;
                case GL_m4fv:
                    GL20.glUniformMatrix4fv(loc, count, false, data);
                    break;

            }
        }

        bindTextures();
    }

    //resize the buffer to allow more/less uniforms
    public void reallocate() {
        ByteBuffer old = data;
        old.rewind();
        data = BufferUtils.createByteBuffer(bufferSize);
        data.put(old);
    }

    private void prepareUniformData(int index) {
        uniformChanged.add(index);
        data.position(uniforms.get(index).offset);
    }

    public void setUniform(int index, ByteBuffer data) {
        ByteBuffer tmp = data.slice();
        tmp.order(ByteOrder.LITTLE_ENDIAN);
        tmp.limit(uniforms.get(index).getByteSize());
        prepareUniformData(index);
        data.put(tmp);
    }

    public void setUniform(int index, float f) {
        prepareUniformData(index);
        data.putFloat(f);
    }

    public void setUniform(int index, float[] fdata) {
        prepareUniformData(index);
        int count = Math.min(uniforms.get(index).getComponents(), fdata.length);
        for (int i = 0; i < count; i++) {
            data.putFloat(fdata[i]);
        }
    }

    public void setUniform(int index, int[] idata) {
        prepareUniformData(index);
        int count = Math.min(uniforms.get(index).getComponents(), idata.length);
        for (int i = 0; i < count; i++) {
            data.putFloat(idata[i]);
        }
    }

    public void setUniform(int index, int i) {
        prepareUniformData(index);
        data.putInt(i);
    }

    public void setUniform(int index, Vector2f v) {
        prepareUniformData(index);
        data.putFloat(v.x).putFloat(v.y);
    }

    public void setUniform(int index, Vector3f v) {
        prepareUniformData(index);
        data.putFloat(v.x).putFloat(v.y).putFloat(v.z);
    }
    
    public void setUniform(int index, Vector4f v) {
        prepareUniformData(index);
        data.putFloat(v.x).putFloat(v.y).putFloat(v.z).putFloat(v.w);
    }

    public void setUniform(int index, Matrix3f m) {
        prepareUniformData(index);
        m.get(data.asFloatBuffer());
    }

    public void setUniform(int index, Matrix4f m) {
        prepareUniformData(index);
        m.get(data.asFloatBuffer());
    }

    public void setUniformBuffer(String blockName, String bufferName) {
        if (!uniformBlocks.contains(blockName)) {
            uniformBlocks.add(blockName);
            owner.addUniformBlock(blockName, bufferName);
        }
    }

    public void setTexture(String samplerName, String textureName) {
        int texUnit = owner.getTextureUnit(samplerName);
        if(!textureMap.containsKey(texUnit)) {
            int uniIndex = createUniform(samplerName, GLType.GL_1iv, 1);
            setUniform(uniIndex, texUnit);
        }
        textureMap.put(texUnit, textureName);

    }

    private void bindTextures() {
        for (Map.Entry<Integer, String> tex : textureMap.entrySet()) {
            RenderManager.getInstance().bind(tex.getValue(), tex.getKey());
        }
    }

    //add uniforms for each texture and color in the material
    //colors are ignored for now
    public void addMaterialUniforms(Material mat) {
        if (mat == null) {
            return;
        }
        for (Map.Entry<String, String> tex : mat.getTextureMap().entrySet()) {
            setTexture(tex.getKey(), tex.getValue());
        }
    }

    private static class Uniform {

        String name;
        int index;
        int location;
        GLType type;
        int count;
        int offset;

        public Uniform(String name, int index, GLType type, int count, int offset) {
            this.name = name;
            this.index = index;
            this.type = type;
            this.count = count;
            this.offset = offset;
        }

        public int getByteSize() {
            return count * type.sizeBytes();
        }
        
        public int getComponents() {
            return count * type.components();
        }
    }
}
