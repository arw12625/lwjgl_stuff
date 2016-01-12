package resource;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author Andy
 * 
 * for loading plain text files
 */
public class TextData extends Data {

    private String textString;
    
    public TextData(){}

    @Override
    public void load(String path) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader br = ResourceManager.getInstance().getReader(path);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            this.textString = sb.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public String getTextString() {
        return textString;
    }

    
    @Override
    public String toString() {
        return getTextString();
    }
    
    public static String loadText(String path) {
        return ResourceManager.getInstance().loadResource(path, new TextData()).getData().getTextString();
    }
}
