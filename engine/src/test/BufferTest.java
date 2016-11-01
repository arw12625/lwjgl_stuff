package test;

/**
 *
 * @author Andrew_2
 * 
 * testing nio buffers
 */
public class BufferTest {
    
    public static void main(String[] args) {
        /*float[] fdata = new float[] {.1454f, .2f, .3f, .4f};
        int[] idata = new int[] {1,2,3,4};
        ByteBuffer data = ByteBuffer.wrap(new byte[]{(byte)62, (byte)20, (byte)-29, (byte)-67, (byte)62, (byte)76,
            (byte)-52, (byte)-51, (byte)62, (byte)-103, (byte)-103, (byte)-102, (byte)62, (byte)-52, (byte)-52,(byte)-51, 
            0,0,0,1,0,0,0,2,0,0,0,3,0,0,0,4
        });
        
        data.rewind();
        System.out.println("d");
        while(data.hasRemaining()) {
            System.out.println(data.get());
        }
        data.rewind();
        
        data.limit(fdata.length * 4);
        FloatBuffer verts = data.slice().asFloatBuffer();
        verts.rewind();
        
        data.position(data.limit());
        data.limit(data.capacity());
        IntBuffer faces = data.slice().asIntBuffer();
        faces.rewind();
        
        System.out.println("v");
        while(verts.hasRemaining()) {
            System.out.println(verts.get());
        }
        
        System.out.println("f");
        while(faces.hasRemaining()) {
            System.out.println(faces.get());
        }
        
        
        System.out.println("HEH");
        ByteBuffer b = ByteBuffer.allocate(48);
        b.putInt(12);
        Matrix3f m = new Matrix3f((float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random());
        m.get(b.asFloatBuffer());
        b.position(b.position() + 4*3*3);
        b.putInt(13);
        b.rewind();
        while(b.hasRemaining()) {
            System.out.println(b.getInt());
        }
        
        
        try{
        FileInputStream fs = new FileInputStream("res/misc_models/index_test.cmf");
            FileChannel channel = fs.getChannel();
            long fsize = channel.size();
            ByteBuffer dat = ByteBuffer.allocate((int) fsize);
            channel.read(dat);
            dat.rewind();
            System.out.println();
            System.out.println(dat.get());
            
        } catch(IOException e) {
            
        }
            System.out.println();
       */
        
        /*float[] fdata = {1, 2, 3, 4};
        ByteBuffer bdata = BufferUtils.createByteBuffer(fdata.length * Float.BYTES);
        bdata.asFloatBuffer().put(fdata);
        bdata.rewind();
        for(int i = 0; i < fdata.length; i++) {
            System.out.println(bdata.getFloat()); 
        }
        bdata.rewind();
        float[] copydata = bdata.asFloatBuffer().array();
        copydata[3] = 2;
        for(int i = 0; i < copydata.length; i++) {
            System.out.println(copydata[i]); 
        }
        List<float[]> f;*/
        
    }
    
    
    
    
}
