package sg.atom.utils.proxy.delegation;

/**
 * Interface indicating that an object is wrapping another object.
 *
 * @author gerald
 * @param <T> Wrapped type
 */
public interface Delegating<T> {

    /**
     * Get wrapped object.
     *
     * @return Wrapped object
     */
    T getDelegate();
}
