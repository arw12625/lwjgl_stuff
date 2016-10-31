package graphics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import org.lwjgl.opengl.GL20;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import org.lwjgl.opengl.GL31;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import resource.TextData;

/**
 *
 * @author Andrew_2
 *
 * Data representing a GLSL shader for opengl Vertex and Fragment shader
 * compiled from text
 *
 * Contains an instance of UniformData to allow syncing of uniforms Uniforms
 * should be updated through UniformData To change the set of uniforms, the
 * uniform data must be swapped
 */
public class ShaderProgram {

    private String vertexText;
    private String fragmentText;
    private int program;
    private boolean created;
    private boolean compiled;

    private Map<String, Integer> uniformLocations;
    private UniformData uniformData;

    private Map<String, Integer> blockLocations;    //map from block name to location
    private Map<String, String> bufferNames;        //map from block name to buffer name
    private Queue<String> blockNamesNew;            //a list of unitialized blocks

    private Map<String, Integer> samplerMap;
    private int texUnitUsed;

    private Map<String, Integer> attributeLocations;
    private int nextAttribLoc;

    public ShaderProgram(String vertexText, String fragmentText) {
        this.vertexText = vertexText;
        this.fragmentText = fragmentText;

        uniformLocations = new HashMap<>();

        blockLocations = new HashMap<>();
        bufferNames = new HashMap<>();
        blockNamesNew = new ConcurrentLinkedQueue<>();

        samplerMap = new HashMap<>();
        texUnitUsed = 0;

        attributeLocations = new HashMap<>();
        nextAttribLoc = 0;
    }

    public int getProgram() {
        return program;
    }

    public void setUniformData(UniformData uni) {
        this.uniformData = uni;
        uni.updateAll();
    }

    public static ShaderProgram loadProgram(String vertexPath, String fragmentPath) {
        String vertexResource = TextData.loadText(vertexPath);
        String fragmentResource = TextData.loadText(fragmentPath);
        ShaderProgram sp = new ShaderProgram(vertexResource, fragmentResource);

        return sp;
    }

    public int createShader() {
        if(!created) {
            this.program =  glCreateProgram();
            created = true;
        }
        return this.program;
    }
    public int compileShader() {
        if (!compiled) {
            this.compiled = true;
            int shaderProgram = this.program;
            int vertexShader = glCreateShader(GL_VERTEX_SHADER);
            int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);

            glShaderSource(vertexShader, vertexText);
            glCompileShader(vertexShader);
            if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
                System.err.println("Vertex shader wasn't able to be compiled correctly. Error log:");
                System.err.println(glGetShaderInfoLog(vertexShader, 1024));
            }
            glShaderSource(fragmentShader, fragmentText);
            glCompileShader(fragmentShader);
            if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
                System.err.println("Fragment shader wasn't able to be compiled correctly. Error log:");
                System.err.println(glGetShaderInfoLog(fragmentShader, 1024));
            }

            glAttachShader(shaderProgram, vertexShader);
            glAttachShader(shaderProgram, fragmentShader);
            glLinkProgram(shaderProgram);
            if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
                System.err.println("Shader program wasn't linked correctly.");
                System.err.println(glGetProgramInfoLog(shaderProgram, 1024));
            }

            glDeleteShader(vertexShader);
            glDeleteShader(fragmentShader);

            this.program = shaderProgram;
        }
        return getProgram();
    }
    
    public int createAndCompileShader() {
        createShader();
        compileShader();
        return this.program;
    }
    
    protected int getAttributeLocation(String name) {
        Integer location = attributeLocations.get(name);
        if (location == null) {
            if (!compiled) {
                location = nextAttribLocation();
                bindAttributeLocation(name, location);
            } else {
                location = GL20.glGetAttribLocation(program, name);
                if(location == -1) {
                    System.err.println("Attribute not defined in shader: " + name);
                } else {
                    addAttributeEntry(name, location);
                }
            }
        }
        return location;
    }

    protected void bindAttributeLocation(String name, int loc) {
        GL20.glBindAttribLocation(program, loc, name);
        addAttributeEntry(name, loc);
    }
    
    private void addAttributeEntry(String name, int location) {
        attributeLocations.put(name, location);
        nextAttribLoc = Math.max(location + 1, nextAttribLoc);
    }
    
    private int nextAttribLocation() {
        return nextAttribLoc;

    }
    
    public void setAttributeDataLocations(AttributeData a) {
        for(Entry<String, Integer> e : attributeLocations.entrySet()) {
            a.setAttributeLocation(e.getKey(), e.getValue());
        }
    }
    

    protected int getUniformLocation(String name) {
        Integer loc = uniformLocations.get(name);
        if (loc == null) {
            loc = GL20.glGetUniformLocation(getProgram(), name);
            if(loc == -1) {
                System.out.println("Uniform not found: " + name);
            }
            uniformLocations.put(name, loc);
        }
        return loc;
    }

    protected void update() {
        if (uniformData != null) {
            uniformData.updateUniforms();
        }
        String blockName;
        while ((blockName = blockNamesNew.poll()) != null) {
            String bufferName = bufferNames.get(blockName);
            int bufferLocation = RenderManager.getInstance().getUniformBuffer(bufferName).getHandle();
            int blockLocation = GL31.glGetUniformBlockIndex(program, blockName);
            blockLocations.put(blockName, blockLocation);
            glBindBufferBase(GL_UNIFORM_BUFFER, 2, bufferLocation);
            GL31.glUniformBlockBinding(program, blockLocation, 2);
        }
    }

    protected void addUniformBlock(String blockName, String bufferName) {
        if (!bufferNames.containsKey(blockName)) {
            bufferNames.put(blockName, bufferName);
            blockNamesNew.add(blockName);
        }
    }

    public int getTextureUnit(String samplerName) {
        Integer texUnit = samplerMap.get(samplerName);
        if (texUnit == null) {
            texUnit = texUnitUsed;
            if (texUnit == 8) {
                System.out.println("TOO MANY TEXTURES");
            }
            texUnitUsed++;
            samplerMap.put(samplerName, texUnit);
        }
        return texUnit;
    }

    public boolean isCompiled() {
        return compiled;
    }

}
