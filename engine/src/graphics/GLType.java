package graphics;

import static org.lwjgl.opengl.GL11.GL_INT;
import static org.lwjgl.opengles.GLES20.GL_FLOAT;

/**
 *
 * @author Andrew_2
 */
public enum GLType {

        GL_1iv(1, GL_INT), GL_1fv(1, GL_FLOAT),
        GL_2iv(2, GL_INT), GL_2fv(2, GL_FLOAT),
        GL_3iv(3, GL_INT), GL_3fv(3, GL_FLOAT),
        GL_4iv(4, GL_INT), GL_4fv(4, GL_FLOAT),
        GL_m2fv(4, GL_FLOAT), GL_m3fv(9, GL_FLOAT), GL_m4fv(16, GL_FLOAT);

        private final int bytesPerEntry;
        private final int components;
        private final int glType;
        private final int sizeBytes;

        GLType(int components, int glType) {
            this.components = components;
            this.glType = glType;
            switch(glType) {
                case GL_INT : this.bytesPerEntry = Integer.BYTES; break;
                case GL_FLOAT : default: this.bytesPerEntry = Float.BYTES; break;
            }
            this.sizeBytes = components * bytesPerEntry;
            
        }

        public int sizeBytes() {
            return sizeBytes;
        }
        
        public int glType() {
            return glType;
        }
        
        public int components() {
            return components;
        }
        
        public int bytesPerEntry() {
            return bytesPerEntry;
        }
    }
