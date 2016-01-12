package graphics;

import org.joml.Vector3f;

/**
 *
 * @author Andrew_2
 * 
 * DirLight contains data for a directional light
 * As this class only contains data, no methods are provided
 */

public class DirLight {
    public Vector3f dir, ambient, diffuse, specular;
    public DirLight(Vector3f dir, Vector3f ambient, Vector3f diffuse, Vector3f specular) {
        this.dir = dir;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
    }
}