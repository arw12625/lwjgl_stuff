package sound;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_close;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_info;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_samples_short_interleaved;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_open_memory;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_stream_length_in_samples;
import org.lwjgl.stb.STBVorbisInfo;
import resource.Data;
import resource.ResourceManager;

/**
 *
 * @author Andrew_2
 * 
 * Represent raw audio data for use with openal
 * load audio files into raw format
 *  supported file formats are raw and ogg
 */
public class SoundData implements Data {

    private ByteBuffer data;
    private int sampleRate = 44100, format = AL10.AL_FORMAT_STEREO16;

    public SoundData() {
    }

    @Override
    public void load(String path, ResourceManager resourceManager) {
        String ext = path.substring(path.lastIndexOf(".") + 1);
        if (ext.equals("ogg")) {
            data = resourceManager.loadResource(path,
                    new resource.BufferData()).getData().getData();
            STBVorbisInfo info = STBVorbisInfo.malloc();
            data = readVorbis(data, info);
            this.sampleRate = info.sample_rate();
            int channels = info.channels();
            int size = info.sizeof();
            if(channels == 1) {
                format = AL10.AL_FORMAT_MONO16;
                /*if(size == 8) {
                    format = AL10.AL_FORMAT_MONO8;
                } else if(size == 24) {
                    format = AL10.AL_FORMAT_MONO16;
                }*/
            } else if (channels == 2) {
                format = AL10.AL_FORMAT_STEREO16;
                /*if(size == 16) {
                    format = AL10.AL_FORMAT_STEREO8;
                } else if(size == 24) {
                    format = AL10.AL_FORMAT_STEREO16;
                }*/
            }
        } else {
            data = resourceManager.loadResource(path,
                    new resource.BufferData()).getData().getData();
        }
    }
    

    @Override
    public void write(String path, ResourceManager resourceManager) {
        throw new UnsupportedOperationException("Writing Sounds is not supported yet"); //To change body of generated methods, choose Tools | Templates.
    }

    public ByteBuffer getData() {
        return data;
    }
    public int getSampleRate() {
        return sampleRate;
    }
    public int getFormat() {
        return format;
    }
    

    private static ByteBuffer readVorbis(ByteBuffer vorbis, STBVorbisInfo info) {
        
        IntBuffer error = BufferUtils.createIntBuffer(1);
        long decoder = stb_vorbis_open_memory(vorbis, error, null);

        stb_vorbis_get_info(decoder, info);

        int channels = info.channels();

        int lengthSamples = stb_vorbis_stream_length_in_samples(decoder) * channels;

        ByteBuffer pcm = BufferUtils.createByteBuffer(lengthSamples * 2);

        stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm, lengthSamples);
        stb_vorbis_close(decoder);
        

        return pcm;
    }

}
