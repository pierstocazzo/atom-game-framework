package sg.atom.utils.repository.cache;

/**
 * The type of a {@link CacheElementRevision}.
 *
 * @author Aidan Morgan
 */
public enum CacheRevisionType {

    /**
     * The {@link CacheElementRevision} represents an add operation.
     */
    ADDED,
    /**
     * The {@link CacheElementRevision} represents a modify operation.
     */
    MODIFIED,
    /**
     * The {@link CacheElementRevision} represents a delete operation.
     */
    DELETED
}
