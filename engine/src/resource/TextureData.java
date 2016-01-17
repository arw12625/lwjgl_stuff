/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package resource;

import graphics.RenderManager;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 *
 * @author Andy
 * 
 * loads image data into a bitmap contained in a buffer
 * used java.imageio
 */
public class TextureData extends Data {

    private ByteBuffer buffer;
    private int width, height;
    private TextureType type;

    public TextureData() {}
    
    public TextureData(ByteBuffer data, int width, int height) {
        this(data, width, height, TextureType.RGBA);
    }
    
    public TextureData(ByteBuffer data, int width, int height, TextureType type) { 
        this.buffer = data;
        this.width = width;
        this.height = height;
        this.type = type;
    }
    
    public TextureType getType() {
        return type;
    }
    
    @Override
    public void load(String path) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(ResourceManager.getInstance().getFile(path));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        width = image.getWidth();
        height = image.getHeight();

        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        buffer = BufferUtils.createByteBuffer(width * height * 4); //4 for RGBA, 3 for RGB
        this.type = TextureType.RGBA;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pixel & 0xFF));               // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
            }
        }

        buffer.flip();

    }
    
    public ByteBuffer getBuffer() {
        return buffer;
    }
    
    public int getImageHeight() {
        return height;
    }

    public int getImageWidth() {
        return width;
    }
    
    public static Resource<TextureData> loadTextureResource(String path) {
        Resource<TextureData> tr = ResourceManager.getInstance().loadResource(path, new TextureData());
        RenderManager.getInstance().queueTexture(tr);
        return tr;
    }
    
    public enum TextureType {
        ALPHA(GL11.GL_ALPHA, GL11.GL_ALPHA), RGB(GL11.GL_RGB, GL11.GL_RGB8), RED(GL11.GL_RED, GL30.GL_R8), RGBA(GL11.GL_RGBA, GL11.GL_RGBA8);
        
        private int glType;
        private int dataType;
        
        private TextureType(int glType, int dataType) {
            this.glType = glType;
            this.dataType = dataType;
        }
        
        public int getGLType() {
            return glType;
        }
        public int getDataType() {
            return glType;
        }
    }
}
