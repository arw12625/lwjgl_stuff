package graphics.particle;

import geometry.Transform;
import java.util.List;
import org.joml.Vector3f;

/**
 *
 * @author Andrew_2
 */
public class PointDistribution implements ParticleDistribution {

    private int lifespan = 1500;
    private float startSpeed = 0.005f;

    Transform t;
    
    public PointDistribution() {
        this(new Vector3f());
    }
    
    public PointDistribution(Vector3f origin) {
        t = new Transform(origin);
    }
    
    @Override
    public void initDistribution(List<SimpleParticle> particles) {
        
    }

    @Override
    public Transform getTransform() {
        return t;
    }

    @Override
    public void updateParticle(int delta, List<SimpleParticle> particles) {

        for (SimpleParticle part : particles) {
            part.setLife(part.getLife() - delta);
            part.size = 0.015f * (float)Math.sqrt(part.life / 10f);
            if (part.getLife() <= 0) {
                for (int j = 0; j < 3; j++) {
                    part.position[j] = 1 * ((float) Math.random() - .5f);
                    part.velocity[j] = startSpeed * ((float) Math.random() - .5f);
                    part.color[j] = (float) Math.random();
                }
                part.life = (int) (Math.random() * lifespan);
                part.color[3] = (float) Math.random();

            }
            for (int j = 0; j < 3; j++) {
                part.getPosition()[j] += part.getVelocity()[j] * delta;
                part.velocity[j] *= .984f;
            }
            part.velocity[1] -= 0.0001f;
        }

    }
}
