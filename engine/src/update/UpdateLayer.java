package update;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import util.ZIndexSet;
import util.ZIndexSetStandard;

/**
 *
 * @author Andrew_2
 */
public class UpdateLayer {
    
    private final Queue<Updateable> updateablesToAdd;
    private final ZIndexSet<Updateable> updateables;
    
    public UpdateLayer(ZIndexSet<Updateable> updateables) {
        this.updateables = updateables;
        updateablesToAdd = new ConcurrentLinkedQueue<>();
    }
    
    public void update(int delta) {
        
        Updateable toInit;
        while((toInit = updateablesToAdd.poll()) != null) {
            toInit.updateInit();
        }
        
        Iterator<Updateable> updateableIterator = updateables.iterator();
        while(updateableIterator.hasNext()) {
            Updateable u = updateableIterator.next();
            
            if(u.isUpdatePendingRelease()) {
                u.updateRelease();
            } else if(u.isUpdateEnabled()) {
                u.update(delta, this);
            }
        }
    }
    
    public void addUpdateable(Updateable u, int zIndex) {
        updateablesToAdd.add(u);
        updateables.add(u, zIndex);
    }
    
    public void removeUpdateable(Updateable u) {
        updateablesToAdd.remove(u);
        updateables.remove(u);
    }
    
    public static UpdateLayer createUpdateLayer() {
        
        return new UpdateLayer(ZIndexSetStandard.<Updateable>createCopyOnWriteSet());
    }
    
}
