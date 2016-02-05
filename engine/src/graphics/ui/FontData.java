package graphics.ui;

import graphics.RenderManager;
import graphics.UniformData;
import graphics.UniformStruct;
import java.nio.ByteBuffer;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;
import resource.Data;
import resource.ResourceManager;
import resource.TextureData;

/**
 *
 * @author Andrew_2
 * 
 * a class representing a true type font and associated bitmap and character map
 */
public class FontData extends Data implements UniformStruct {

    private String name;
    private String textureName;
    private TextureData tex;
    private STBTTBakedChar.Buffer cdata;
    private int fontSize;
    private Vector4f color;
    private int bitMapWidth, bitMapHeight;

    public static final int defaultBitmapWidth = 512;
    public static final int defaultBitmapHeight = 512;
    
    public static final String colorUniformName = "color", textureUniformName = "tex";

    public FontData(String name, int fontSize, int bitMapWidth, int bitMapHeight, Vector4f color) {
        this.name = name;
        this.fontSize = fontSize;
        this.color = color;
        this.bitMapHeight = bitMapHeight;
        this.bitMapWidth = bitMapWidth;
    }
    
    @Override
    public void load(String path) {
        ByteBuffer ttf = ResourceManager.getInstance().loadBuffer(path);
        ByteBuffer bitmap = BufferUtils.createByteBuffer(bitMapHeight * bitMapWidth);
        cdata = STBTTBakedChar.createBuffer(96);
        STBTruetype.stbtt_BakeFontBitmap(ttf, fontSize, bitmap, bitMapWidth, bitMapHeight, 32, cdata);
        tex = new TextureData(bitmap, bitMapWidth, bitMapHeight, TextureData.TextureType.ALPHA);
        textureName = name + "_" + fontSize;
        RenderManager.getInstance().queueTexture(textureName, tex);
    }

    @Override
    public void createUniformStruct(UniformData parent) {
        int id = parent.createUniform("color", UniformData.GL_UNIFORM_TYPE.GL_4fv, 1);
        parent.setUniform(id, color);
        parent.setTexture(textureUniformName, textureName);
    }

    @Override
    public void updateUniformStruct(UniformData parent) {
        //color is final
    }

    public String getName() {
        return name;
    }

    public STBTTBakedChar.Buffer getCdata() {
        return cdata;
    }

    public int getFontSize() {
        return fontSize;
    }

    public int getBitMapWidth() {
        return bitMapWidth;
    }

    public int getBitMapHeight() {
        return bitMapHeight;
    }
    
}
