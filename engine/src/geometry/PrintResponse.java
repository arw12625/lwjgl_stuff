package geometry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andrew_2
 */
public class PrintResponse implements CollisionResponse {

    private static final Logger LOG = LoggerFactory.getLogger(PrintResponse.class);
    
    public PrintResponse() {
        
    }
    
    @Override
    public void respond(CollisionData data) {
        LOG.debug("{}",data);
    }
    
}
