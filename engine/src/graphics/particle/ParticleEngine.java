package graphics.particle;

import game.Component;
import graphics.RenderManager;
import graphics.Renderable;
import graphics.VAORender;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew_2
 *
 * A ParticleEngine consists of a grouping of ParticleEmitters. All
 * ParticleEmitters in a single ParticleEngine must use the same VAO.
 *
 */
public class ParticleEngine extends Renderable implements update.Updateable {

    String name;
    List<ParticleEmitter> instances;
    VAORender vao;

    int offset;

    public ParticleEngine(Component parent, String name, VAORender vao) {

        super(parent);
        this.name = name;
        this.vao = vao;

        instances = new ArrayList<>();

    }

    @Override
    public int getZIndex() {
        return RenderManager.PRE_RENDER_Z_INDEX;
    }

    @Override
    public void initRender() {

        vao.generateVAO();

    }

    @Override
    public void render() {
        RenderManager.getInstance().useAndUpdateVAO(vao);

    }

    public VAORender getVAO() {
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
    public void update(int delta) {

        for (ParticleEmitter pe : instances) {
            pe.update(delta);
        }
    }

}
