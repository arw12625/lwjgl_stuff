package resource;

import game.StandardGame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andy
 * 
 * for loading plain text files
 */
public class TextData implements Data {

    private String textString;
    
    private static final Logger LOG = LoggerFactory.getLogger(TextData.class);
    
    public TextData(){}

    @Override
    public void load(String path, ResourceManager resourceManager) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader br = resourceManager.getReader(path);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            this.textString = sb.toString();
        } catch (IOException ex) {
            LOG.error("{}",ex);
        }
    }
    
    @Override
    public void write(String path, ResourceManager resourceManager) {
        try {
            
            BufferedWriter bw = resourceManager.getWriter(path);
            bw.write(textString);
            
        } catch (IOException ex) {
            LOG.error("{}",ex);
        }
    }
    
    public String getTextString() {
        return textString;
    }

    @Override
    public boolean isValid() {
        return textString != null;
    }
    
    @Override
    public String toString() {
        return getTextString();
    }
    
    public static String loadText(String path, StandardGame game) {
        return loadText(path, game.getResourceManager());
    }
    
    public static String loadText(String path, ResourceManager resourceManager) {
        return resourceManager.loadResource(path, new TextData()).getData().getTextString();
    }

}
