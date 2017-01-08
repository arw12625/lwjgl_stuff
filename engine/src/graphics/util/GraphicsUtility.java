package graphics.util;

import graphics.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andrew_2
 */
public class GraphicsUtility {
    
    private static final Logger LOG = LoggerFactory.getLogger(GraphicsUtility.class);
    
    public static Camera getHackyCamera(View v) {
        if(v instanceof HasCamera) {
            return ((HasCamera)v).getCamera();
        } else {
            LOG.error("View does not have camera: {}", v);
            return null;
        }
        
    }
    
}
