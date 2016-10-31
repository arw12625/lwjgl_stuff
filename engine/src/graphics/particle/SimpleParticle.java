package graphics.particle;

import java.nio.ByteBuffer;
import util.Bufferable;

/**
 *
 * @author Andrew_2
 */
public class SimpleParticle extends Particle {

    float size; 
    float[] color;
    
    
    public SimpleParticle() {
        color = new float[4];
    }
    
    @Override
    public void write(ByteBuffer b) {
        b.putFloat(size);
        b.putFloat(position[0]).putFloat(position[1]).putFloat(position[2]);
        b.putFloat(color[0]).putFloat(color[1]).putFloat(color[2]).putFloat(color[3]);
    }
}
