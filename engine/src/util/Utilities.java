/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andy
 * 
 * A class for future utilities
 */
public final class Utilities {

    private Utilities(){}
    
    private static final Logger LOG = LoggerFactory.getLogger(Utilities.class);
    
    public static void putVector4f(ByteBuffer b, Vector4f v) {
        b.putFloat(v.x).putFloat(v.y).putFloat(v.z).putFloat(v.w);
    }

    public static void putVector3f(ByteBuffer b, Vector3f v) {
        b.putFloat(v.x).putFloat(v.y).putFloat(v.z);
    }
    
    public static void putVector2f(ByteBuffer b, Vector2f v) {
        b.putFloat(v.x).putFloat(v.y);
    }

    
    public static void printFloatBuffer(FloatBuffer buf) {
        FloatBuffer copy = buf.duplicate();
        StringBuilder out = new StringBuilder();
        while(copy.hasRemaining()) {
            out.append(copy.get() + " ");
        }
        out.append("\n");
        LOG.debug(out.toString());
    }
    
    public static void printByteBuffer(ByteBuffer buf) {
        ByteBuffer copy = buf.duplicate();
        StringBuilder out = new StringBuilder();
        while(copy.hasRemaining()) {
            out.append(copy.get() + " ");
        }
        out.append("\n");
        LOG.debug(out.toString());
    }
    
}
