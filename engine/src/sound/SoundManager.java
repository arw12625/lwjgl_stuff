package sound;

import java.util.HashMap;
import java.util.Map;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALContext;
import org.lwjgl.openal.ALDevice;

/**
 *
 * @author Andrew_2
 *
 * SoundManager is an interface with openal allowing basic audio functionality
 * Audio must be loaded into buffers and then attached to sources to play
 */
public class SoundManager {

    ALDevice dev;
    ALContext context;

    Map<String, Integer> sources;
    Map<String, Integer> buffers;

    public SoundManager() {
        sources = new HashMap<>();
        buffers = new HashMap<>();

    }

    public void initialize() {
        context = ALContext.create();
        context.makeCurrent();
    }

    public void release() {
        context.destroy();
        context.getDevice().destroy();
    }

    public String loadBuffer(String name, SoundData data) {
        return loadBuffer(name, data, false);
    }

    public String loadBuffer(String name, SoundData data, boolean reload) {
        if (!buffers.containsKey(name) || reload) {
            int handle = AL10.alGenBuffers();
            AL10.alBufferData(handle, data.getFormat(), data.getData(), data.getSampleRate());
            buffers.put(name, handle);
        }
        return name;
    }

    public String createSource(String name) {
        return createSource(name, false);
    }

    public String createSource(String name, boolean replace) {
        if (!sources.containsKey(name) || replace) {
            int handle = AL10.alGenSources();
            sources.put(name, handle);

            AL10.alSourcef(handle, AL10.AL_PITCH, 1.0f);
            AL10.alSourcef(handle, AL10.AL_GAIN, 1.0f);
            AL10.alSource3f(handle, AL10.AL_POSITION, 0, 0, 0);
            AL10.alSource3f(handle, AL10.AL_VELOCITY, 0, 0, 0);

        }
        return name;
    }

    public void setSourcePosition(String name, float x, float y, float z) {
        int handle = sources.get(name);
        AL10.alSource3f(handle, AL10.AL_POSITION, x, y, z);
    }

    public void setSourceVelocity(String name, float x, float y, float z) {
        int handle = sources.get(name);
        AL10.alSource3f(handle, AL10.AL_VELOCITY, x, y, z);
    }

    public void setSourceGain(String name, float gain) {
        int handle = sources.get(name);
        AL10.alSourcef(handle, AL10.AL_GAIN, 1.0f);
    }

    public void setListenerPosition(Vector3f v) {
        setListenerPosition(v.x, v.y, v.z);
    }

    public void setListenerPosition(float x, float y, float z) {
        AL10.alListener3f(AL10.AL_POSITION, x, y, z);
    }

    public void setListenerVelocity(float x, float y, float z) {
        AL10.alListener3f(AL10.AL_VELOCITY, x, y, z);
    }

    public void setSourceBuffer(String sourceName, String bufferName, boolean play, boolean loop) {
        int sourceHandle = sources.get(sourceName);
        int bufferHandle = buffers.get(bufferName);

        AL10.alSourcei(sourceHandle, AL10.AL_BUFFER, bufferHandle);

        AL10.alSourcei(sourceHandle, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);

        if (play) {
            AL10.alSourcePlay(sourceHandle);
        }

    }

    public void queueSourceBuffer(String sourceName, String bufferName) {
        int sourceHandle = sources.get(sourceName);
        int bufferHandle = buffers.get(bufferName);

        AL10.alSourceQueueBuffers(sourceHandle, bufferHandle);
    }

    public void playSource(String name) {
        int handle = sources.get(name);
        AL10.alSourcePlay(handle);
    }

    public void restart(String name) {
        int handle = sources.get(name);
        AL10.alSourceRewind(handle);
        AL10.alSourcePlay(handle);
    }

    public void pauseSource(String name) {
        int handle = sources.get(name);
        AL10.alSourcePause(handle);
    }

    public void stopSource(String name) {
        int handle = sources.get(name);
        AL10.alSourceStop(handle);
    }
}
