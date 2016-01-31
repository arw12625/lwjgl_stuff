/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.nio.ByteBuffer;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 *
 * @author Andy
 * 
 * A class for future utilities
 */
public class Utilities {

    public static void putVector4f(ByteBuffer b, Vector4f v) {
        b.putFloat(v.x).putFloat(v.y).putFloat(v.z).putFloat(v.w);
    }

    public static void putVector3f(ByteBuffer b, Vector3f v) {
        b.putFloat(v.x).putFloat(v.y).putFloat(v.z);
    }
    
    public static void putVector2f(ByteBuffer b, Vector2f v) {
        b.putFloat(v.x).putFloat(v.y);
    }

    
}
