package util;

import java.util.Collection;
import java.util.Set;

/**
 *
 * @author Andrew_2
 */
public interface ZIndexSet<T> extends Set<T>  {
    
    public boolean add(T t, int index);
    
    public void sort();
    
    @Override
    public default boolean add(T e) {
        throw new UnsupportedOperationException("Cannot add element without zIndex");
    }

    @Override
    public default boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("Cannot add element without zIndex");
    }
    
}
