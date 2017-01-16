package graphics.util;

import graphics.View;
import java.util.Arrays;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andrew_2
 */
public class GraphicsUtility {

    private static final Logger LOG = LoggerFactory.getLogger(GraphicsUtility.class);

    public static Camera getHackyCamera(View v) {
        if (v instanceof HasCamera) {
            return ((HasCamera) v).getCamera();
        } else {
            LOG.error("View does not have camera: {}", v);
            return null;
        }

    }

    private static final float[] cubeVerts = {
        1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, 1.0f,
        -1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,
        -1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, 1.0f,};

    public static float[] getCubeVerts() {
        return Arrays.copyOf(cubeVerts, cubeVerts.length);
    }

    public static Vector3f[] getCubeVertsVector() {
        Vector3f[] coords = new Vector3f[cubeVerts.length / 3];

        for (int i = 0; i < coords.length; i++) {
            coords[i] = new Vector3f(
                    cubeVerts[3 * i],
                    cubeVerts[3 * i + 1],
                    cubeVerts[3 * i + 2]);
        }
        return coords;
    }

    //all indices are given so that normals point exterior of the cube
    private static final int[] cubeTriStrip = {
        1, 2, 5,
        6, 1, 3,
        7, 1, 4,
        6, 4, 1,
        1, 5, 3,
        7, 2, 1,
        5, 2, 7,
        0, 4, 6,
        4, 0, 7,
        0, 6, 3,
        0, 3, 5,
        0, 5, 7
    };

    public static final int[] getCubeTriStripIndices() {
        return Arrays.copyOf(cubeTriStrip, cubeTriStrip.length);
    }

    private static final int[] cubeQuads = {
        1, 4, 7, 2,
        1, 3, 6, 4,
        1, 2, 5, 3,
        0, 6, 3, 5,
        0, 5, 2, 7,
        0, 7, 4, 6
    };

    public static final int[] getCubeQuadsIndices() {
        return Arrays.copyOf(cubeQuads, cubeQuads.length);
    }

    private static final int[] cubeLines = {
        1, 2,
        2, 7,
        7, 4,
        4, 1,
        1, 3,
        2, 5,
        7, 0,
        4, 6,
        3, 5,
        5, 0,
        0, 6,
        6, 3
    };

    public static final int[] getCubeLinesIndices() {
        return Arrays.copyOf(cubeLines, cubeLines.length);
    }

    public static final Vector3f getRandomVector3f() {
        double u1 = 1-2*Math.random();
        double u2 = 1-2*Math.random();
        double u3 = 1-2*Math.random();
        return new Vector3f((float)u1, (float)u2, (float)u3);

    }
    
    public static final Quaternionf getRandomQuaternionf() {
        double u1 = Math.random();
        double u2 = Math.random();
        double u3 = Math.random();

        return new Quaternionf(
                (float) (Math.sqrt(1 - u1) * Math.sin(2 * Math.PI * u2)),
                (float) (Math.sqrt(1 - u1) * Math.cos(2 * Math.PI * u2)),
                (float) (Math.sqrt(u1) * Math.sin(2 * Math.PI * u3)),
                (float) (Math.sqrt(u1) * Math.cos(2 * Math.PI * u3))
        );
    }

}
