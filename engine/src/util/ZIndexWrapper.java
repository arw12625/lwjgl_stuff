package util;

/**
 *
 * @author Andrew_2
 */
//the ordering of layers
public class ZIndexWrapper<T> implements Comparable<ZIndexWrapper<T>> {

    private T t;
    private int zIndex;

    private ZIndexWrapper(T t, int zIndex) {
        this.t = t;
        this.zIndex = zIndex;
    }

    @Override
    public int compareTo(ZIndexWrapper<T> o) {
        return zIndex - o.zIndex;
    }

    public T getObject() {
        return t;
    }
    
    public int getZIndex() {
        return zIndex;
    }
    
}
