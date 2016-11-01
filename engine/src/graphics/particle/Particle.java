package graphics.particle;

import util.Bufferable;

/**
 *
 * @author Andrew_2
 * 
 * A Particle is a discrete particle in space with a duration (life), position, and velocity.
 * Particles are created from a ParticleEmitter
 * 
 */
public abstract class Particle implements Bufferable {
    
    int life;
    float[] position;
    float[] velocity;
    
    public Particle() {
        position = new float[3];
        velocity = new float[3];
    }
    
    int getLife() {
        return life;
    }
    void setLife(int life) {
        this.life = life;
    }
    
    float[] getPosition() {
        return position;
    }

    void setPosition(float[] position) {
        this.position = position;
    }

    float[] getVelocity() {
        return velocity;
    }

    void setVelocity(float[] velocity) {
        this.velocity = velocity;
    }
}
