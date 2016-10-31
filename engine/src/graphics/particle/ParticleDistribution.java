package graphics.particle;

import geometry.Transform;
import java.util.List;

/**
 *
 * @author Andrew_2
 */
public interface ParticleDistribution {

    public void initDistribution(List<SimpleParticle> particles);

    public void updateParticle(int delta, List<SimpleParticle> particles);
    

    public Transform getTransform();
    
}
