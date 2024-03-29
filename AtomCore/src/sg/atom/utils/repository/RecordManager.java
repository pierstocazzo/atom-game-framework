package sg.atom.utils.repository;

import java.io.IOException;
import sg.atom.utils.datastructure.serializable.Serializer;

/**
 * An interface to manages records, which are uninterpreted blobs of data. <p>
 * The set of record operations is simple: fetch, insert, update and delete.
 * Each record is identified using a "rowid" and contains a byte[] data block.
 * Rowids are returned on inserts and you can store them someplace safe to be
 * able to get back to them. Data blocks can be as long as you wish, and may
 * have lengths different from the original when updating.
 *
 * @author <a href="mailto:boisvert@intalio.com">Alex Boisvert</a>
 * @author <a href="cg@cdegroot.com">Cees de Groot</a>
 * @version $Id: RecordManager.java,v 1.3 2005/06/25 23:12:31 doomdark Exp $
 */
public interface RecordManager {

    /**
     * Reserved slot for name directory.
     */
    public static final int NAME_DIRECTORY_ROOT = 0;

    /**
     * Inserts a new record using standard java object serialization.
     *
     * @param obj the object for the new record.
     * @return the rowid for the new record.
     * @throws IOException when one of the underlying I/O operations fails.
     */
    public abstract long insert(Object obj)
            throws IOException;

    /**
     * Inserts a new record using a custom serializer.
     *
     * @param obj the object for the new record.
     * @param serializer a custom serializer
     * @return the rowid for the new record.
     * @throws IOException when one of the underlying I/O operations fails.
     */
    public abstract long insert(Object obj, Serializer serializer)
            throws IOException;

    /**
     * Deletes a record.
     *
     * @param recid the rowid for the record that should be deleted.
     * @throws IOException when one of the underlying I/O operations fails.
     */
    public abstract void delete(long recid)
            throws IOException;

    /**
     * Updates a record using standard java object serialization.
     *
     * @param recid the recid for the record that is to be updated.
     * @param obj the new object for the record.
     * @throws IOException when one of the underlying I/O operations fails.
     */
    public abstract void update(long recid, Object obj)
            throws IOException;

    /**
     * Updates a record using a custom serializer.
     *
     * @param recid the recid for the record that is to be updated.
     * @param obj the new object for the record.
     * @param serializer a custom serializer
     * @throws IOException when one of the underlying I/O operations fails.
     */
    public abstract void update(long recid, Object obj, Serializer serializer)
            throws IOException;

    /**
     * Fetches a record using standard java object serialization.
     *
     * @param recid the recid for the record that must be fetched.
     * @return the object contained in the record.
     * @throws IOException when one of the underlying I/O operations fails.
     */
    public abstract Object fetch(long recid)
            throws IOException;

    /**
     * Fetches a record using a custom serializer.
     *
     * @param recid the recid for the record that must be fetched.
     * @param serializer a custom serializer
     * @return the object contained in the record.
     * @throws IOException when one of the underlying I/O operations fails.
     */
    public abstract Object fetch(long recid, Serializer serializer)
            throws IOException;

    /**
     * Closes the record manager.
     *
     * @throws IOException when one of the underlying I/O operations fails.
     */
    public abstract void close()
            throws IOException;

    /**
     * Returns the number of slots available for "root" rowids. These slots can
     * be used to store special rowids, like rowids that point to other rowids.
     * Root rowids are useful for bootstrapping access to a set of data.
     */
    public abstract int getRootCount();

    /**
     * Returns the indicated root rowid.
     *
     * @see #getRootCount
     */
    public abstract long getRoot(int id)
            throws IOException;

    /**
     * Sets the indicated root rowid.
     *
     * @see #getRootCount
     */
    public abstract void setRoot(int id, long rowid)
            throws IOException;

    /**
     * Commit (make persistent) all changes since beginning of transaction.
     */
    public abstract void commit()
            throws IOException;

    /**
     * Rollback (cancel) all changes since beginning of transaction.
     */
    public abstract void rollback()
            throws IOException;

    /**
     * Obtain the record id of a named object. Returns 0 if named object doesn't
     * exist.
     */
    public abstract long getNamedObject(String name)
            throws IOException;

    /**
     * Set the record id of a named object.
     */
    public abstract void setNamedObject(String name, long recid)
            throws IOException;
}
