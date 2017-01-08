package util;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Andrew_2
 */
public class ZIndexSetStandard<T> implements ZIndexSet<T> {

    private final List<T> objects;
    private final Map<T, Integer> indices;
    private final Comparator<T> comparator;

    private boolean isSorted;

    public ZIndexSetStandard(List<T> objects, Map<T, Integer> indices) {
        this.objects = objects;
        this.indices = indices;
        this.comparator = new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                return indices.get(o1).compareTo(indices.get(o2));
            }
        };
    }

    @Override
    public boolean add(T t, int index) {
        isSorted = false;
        indices.put(t, index);
        objects.add(t);
        return true;
    }

    @Override
    public void sort() {
        if (!isSorted) {
            objects.sort(comparator);
            isSorted = true;
        }
    }

    @Override
    public int size() {
        return objects.size();
    }

    @Override
    public boolean isEmpty() {
        return objects.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return objects.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return objects.iterator();
    }

    @Override
    public Object[] toArray() {
        return objects.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return objects.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        boolean removed = objects.remove(o);
        indices.remove(o);
        return removed;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return objects.containsAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = objects.retainAll(c);
        if (changed) {
            Set<T> difference = new HashSet<>(indices.keySet());
            difference.removeAll(objects);
            indices.remove(difference);
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = objects.remove(c);
        indices.remove(c);
        return changed;
    }

    @Override
    public void clear() {
        objects.clear();
        indices.clear();
    }

    public static <T> ZIndexSetStandard<T> createCopyOnWriteSet() {
        return new ZIndexSetStandard<>(
                new CopyOnWriteArrayList(),
                new ConcurrentHashMap<>());
    }
}
