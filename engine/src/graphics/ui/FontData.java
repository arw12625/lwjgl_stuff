package graphics.ui;

import game.StandardGame;
import graphics.GLType;
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
public class FontData implements UniformStruct, Data {

    private String name;
    private String textureName;
    private TextureData tex;
    private STBTTBakedChar.Buffer cdata;
    private int fontSize;
    private Vector4f color;
    private int bitMapWidth, bitMapHeight;
    private RenderManager renderManager;

    public static final int defaultBitmapWidth = 512;
    public static final int defaultBitmapHeight = 512;
    
    public static final String colorUniformName = "color", textureUniformName = "tex";

    public FontData() {
        
    }
    
    public FontData(String name, int fontSize, int bitMapWidth, int bitMapHeight, Vector4f color, RenderManager renderManager) {
        this.name = name;
        this.fontSize = fontSize;
        this.color = color;
        this.bitMapHeight = bitMapHeight;
        this.bitMapWidth = bitMapWidth;
        this.renderManager = renderManager;
    }
    
    @Override
    public void load(String path, ResourceManager resourceManager) {
        ByteBuffer ttf = resourceManager.loadBuffer(path);
        ByteBuffer bitmap = BufferUtils.createByteBuffer(bitMapHeight * bitMapWidth);
        cdata = STBTTBakedChar.createBuffer(96);
        STBTruetype.stbtt_BakeFontBitmap(ttf, fontSize, bitmap, bitMapWidth, bitMapHeight, 32, cdata);
        tex = new TextureData(bitmap, bitMapWidth, bitMapHeight, TextureData.TextureType.ALPHA);
        textureName = name + "_" + fontSize;
        renderManager.queueTexture(textureName, tex);
    }
    
    public static FontData loadFont(String path, String name, int fontSize, int bitMapWidth, int bitMapHeight, Vector4f color, StandardGame game) {
        return loadFont(path, name, fontSize, bitMapWidth, bitMapHeight, color, game.getRenderManager(), game.getResourceManager());
    }
    public static FontData loadFont(String path, String name, int fontSize, int bitMapWidth, int bitMapHeight, Vector4f color, RenderManager renderManager, ResourceManager resourceManager) {
        FontData font = new FontData(name, fontSize, bitMapWidth, bitMapHeight, color, renderManager);
        resourceManager.loadResource(path, font);
        return font;
    }
    
    @Override
    public void write(String path, ResourceManager resourceManager) {
        throw new UnsupportedOperationException("Writing font files is not supported.\nThis isn't a font editing program... yet.");
    }

    @Override
    public void createUniformStruct(UniformData parent) {
        int id = parent.createUniform("color", GLType.GL_4fv, 1);
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
