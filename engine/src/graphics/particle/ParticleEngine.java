package graphics.particle;

import graphics.RenderLayer;
import graphics.VAOAttributes;
import graphics.View;
import graphics.util.RenderableUpdateableAdapter;
import java.util.ArrayList;
import java.util.List;
import update.UpdateLayer;

/**
 *
 * @author Andrew_2
 *
 * A ParticleEngine consists of a grouping of ParticleEmitters. All
 * ParticleEmitters in a single ParticleEngine must use the same VAO.
 *
 */
public class ParticleEngine extends RenderableUpdateableAdapter {

    String name;
    List<ParticleEmitter> instances;
    VAOAttributes vao;

    int offset;

    public ParticleEngine(String name, VAOAttributes vao) {

        this.name = name;
        this.vao = vao;

        instances = new ArrayList<>();

    }

    @Override
    public void renderInit() {

        vao.generateVAO();

    }

    @Override
    public void render(View view, RenderLayer layer) {
        vao.useAndUpdateVAO();

    }

    public VAOAttributes getVAO() {
        return vao;
    }

    public void addParticleEmitter(ParticleEmitter pe) {
        pe.init(vao, offset);
        instances.add(pe);
        offset += pe.getByteSize();
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public void update(int delta, UpdateLayer layer) {

        for (ParticleEmitter pe : instances) {
            pe.update(delta);
        }
    }

}
