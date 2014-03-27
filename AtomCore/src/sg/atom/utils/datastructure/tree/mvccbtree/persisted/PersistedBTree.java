/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package sg.atom.utils.datastructure.tree.mvccbtree.persisted;


import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.CacheConfiguration;

import sg.atom.utils.datastructure.tree.mvccbtree.exception.KeyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sg.atom.utils.datastructure.tree.mvccbtree.AbstractBTree;
import sg.atom.utils.datastructure.tree.mvccbtree.AbstractPage;
import sg.atom.utils.datastructure.tree.mvccbtree.BTree;
import sg.atom.utils.datastructure.tree.mvccbtree.BTreeHeader;
import sg.atom.utils.datastructure.tree.mvccbtree.BTreeTypeEnum;
import sg.atom.utils.datastructure.tree.mvccbtree.results.DeleteResult;
import sg.atom.utils.datastructure.tree.mvccbtree.results.InsertResult;
import sg.atom.utils.datastructure.tree.mvccbtree.results.ModifyResult;
import sg.atom.utils.datastructure.tree.mvccbtree.results.NotPresentResult;
import sg.atom.utils.datastructure.tree.mvccbtree.Page;
import sg.atom.utils.datastructure.tree.mvccbtree.PageHolder;
import sg.atom.utils.datastructure.tree.mvccbtree.ReadTransaction;
import sg.atom.utils.datastructure.tree.mvccbtree.RecordManager;
import sg.atom.utils.datastructure.tree.mvccbtree.results.RemoveResult;
import sg.atom.utils.datastructure.tree.mvccbtree.SplitResult;
import sg.atom.utils.datastructure.collection.Tuple;
import sg.atom.utils.datastructure.tree.mvccbtree.WriteTransaction;


/**
 * The B+Tree MVCC data structure.
 *
 * @param <K> The type for the keys
 * @param <V> The type for the stored values
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PersistedBTree<K, V> extends AbstractBTree<K, V> implements Closeable
{
    /** The LoggerFactory used by this class */
    protected static final Logger LOG = LoggerFactory.getLogger( PersistedBTree.class );

    /** The RecordManager if the BTree is managed */
    private RecordManager recordManager;

    /** The cache associated with this BTree */
    protected Cache cache;

    /** The default number of pages to keep in memory */
    public static final int DEFAULT_CACHE_SIZE = 1000;

    /** The cache size, default to 1000 elements */
    protected int cacheSize = DEFAULT_CACHE_SIZE;

    /** A flag indicating if this BTree is a Sub BTree */
    private boolean isSubBtree = false;

    /** The number of stored Values before we switch to a BTree */
    private static final int DEFAULT_VALUE_THRESHOLD_UP = 8;

    /** The number of stored Values before we switch back to an array */
    private static final int DEFAULT_VALUE_THRESHOLD_LOW = 1;

    /** The configuration for the array <-> BTree switch */
    public static int valueThresholdUp = DEFAULT_VALUE_THRESHOLD_UP;
    public static int valueThresholdLow = DEFAULT_VALUE_THRESHOLD_LOW;

    /** A lock to protect the creation of the transaction */
    protected ReentrantLock createTransaction = new ReentrantLock();


    /**
     * Creates a new BTree, with no initialization.
     */
    public PersistedBTree()
    {
        btreeHeader = new BTreeHeader();
        setType( BTreeTypeEnum.PERSISTED );
    }


    /**
     * Creates a new persisted BTree using the BTreeConfiguration to initialize the
     * BTree
     *
     * @param configuration The configuration to use
     */
    public PersistedBTree( PersistedBTreeConfiguration<K, V> configuration )
    {
        super();
        String name = configuration.getName();

        if ( name == null )
        {
            throw new IllegalArgumentException( "BTree name cannot be null" );
        }

        btreeHeader = new BTreeHeader();
        btreeHeader.setName( name );
        btreeHeader.setPageSize( configuration.getPageSize() );
        isSubBtree = configuration.isSubBtree();

        keySerializer = configuration.getKeySerializer();
        btreeHeader.setKeySerializerFQCN( keySerializer.getClass().getName() );

        valueSerializer = configuration.getValueSerializer();
        btreeHeader.setValueSerializerFQCN( valueSerializer.getClass().getName() );

        readTimeOut = configuration.getReadTimeOut();
        writeBufferSize = configuration.getWriteBufferSize();
        btreeHeader.setAllowDuplicates( configuration.isAllowDuplicates() );
        cacheSize = configuration.getCacheSize();
        setType( BTreeTypeEnum.PERSISTED );

        if ( keySerializer.getComparator() == null )
        {
            throw new IllegalArgumentException( "Comparator should not be null" );
        }

        // Create the first root page, with revision 0L. It will be empty
        // and increment the revision at the same time
        rootPage = new PersistedLeaf<K, V>( this );

        if ( isSubBtree )
        {
            // The subBTree inherit its cache from its parent BTree
            this.cache = ( ( PersistedBTree<K, V> ) configuration.getParentBTree() ).getCache();
            this.writeLock = ( ( PersistedBTree<K, V> ) configuration.getParentBTree() ).getWriteLock();
            readTransactions = new ConcurrentLinkedQueue<ReadTransaction<K, V>>();
        }

        // Now, initialize the BTree
        init();
    }


    /**
     * Initialize the BTree.
     *
     * @throws IOException If we get some exception while initializing the BTree
     */
    public void init()
    {
        if ( !isSubBtree )
        {
            // This is not a subBtree, we have to initialize the cache

            // Create the queue containing the pending read transactions
            readTransactions = new ConcurrentLinkedQueue<ReadTransaction<K, V>>();

            writeLock = new ReentrantLock();

            // Initialize the caches
            CacheConfiguration cacheConfiguration = new CacheConfiguration();
            cacheConfiguration.setName( "pages" );
            cacheConfiguration.setEternal( true );
            cacheConfiguration.setOverflowToDisk( false );
            cacheConfiguration.setCacheLoaderTimeoutMillis( 0 );
            cacheConfiguration.setMaxElementsInMemory( cacheSize );
            cacheConfiguration.setMemoryStoreEvictionPolicy( "LRU" );

            cache = new Cache( cacheConfiguration );
            cache.initialise();
        }

        // Initialize the txnManager thread
        //FIXME we should NOT create a new transaction manager thread for each BTree
        //createTransactionManager();
    }


    /**
     * Return the cache we use in this BTree
     */
    public Cache getCache()
    {
        return cache;
    }


    /**
     * Return the cache we use in this BTree
     */
    public ReentrantLock getWriteLock()
    {
        return writeLock;
    }


    /**
     * Return the cache we use in this BTree
     */
    public ConcurrentLinkedQueue<ReadTransaction<K, V>> getReadTransactions()
    {
        return readTransactions;
    }


    /**
     * Close the BTree, cleaning up all the data structure
     */
    public void close() throws IOException
    {
        // Stop the readTransaction thread
        // readTransactionsThread.interrupt();
        // readTransactions.clear();

        // Clean the cache
        if ( cache.getStatus() == Status.STATUS_ALIVE )
        {
            cache.removeAll();
        }

        cache.dispose();

        rootPage = null;
    }


    /**
     * @return the btreeOffset
     */
    public long getBtreeOffset()
    {
        return btreeHeader.getBTreeOffset();
    }


    /**
     * @param btreeOffset the btreeOffset to set
     */
    public void setBtreeOffset( long btreeOffset )
    {
        btreeHeader.setBTreeOffset( btreeOffset );
    }


    /**
     * @return the rootPageOffset
     */
    public long getRootPageOffset()
    {
        return btreeHeader.getRootPageOffset();
    }


    /**
     * @param rootPageOffset the rootPageOffset to set
     */
    public void setRootPageOffset( long rootPageOffset )
    {
        btreeHeader.setRootPageOffset( rootPageOffset );
    }


    /**
     * @return the nextBTreeOffset
     */
    public long getNextBTreeOffset()
    {
        return btreeHeader.getNextBTreeOffset();
    }


    /**
     * @param nextBTreeOffset the nextBTreeOffset to set
     */
    public void setNextBTreeOffset( long nextBTreeOffset )
    {
        btreeHeader.setNextBTreeOffset( nextBTreeOffset );
    }


    /**
     * Gets the RecordManager for a managed BTree
     *
     * @return The recordManager if the BTree is managed
     */
    public RecordManager getRecordManager()
    {
        return recordManager;
    }


    /**
     * Inject a RecordManager for a managed BTree
     *
     * @param recordManager The injected RecordManager
     */
    public void setRecordManager( RecordManager recordManager )
    {
        this.recordManager = recordManager;
    }


    /**
     *
     * Deletes the given <key,value> pair if both key and value match. If the given value is null
     * and there is no null value associated with the given key then the entry with the given key
     * will be removed.
     *
     * @param key The key to be removed
     * @param value The value to be removed (can be null, and when no null value exists the key will be removed irrespective of the value)
     * @param revision The revision to be associated with this operation
     * @return
     * @throws IOException
     */
    protected Tuple<K, V> delete( K key, V value, long revision ) throws IOException
    {
        writeLock.lock();

        try
        {
            // If the key exists, the existing value will be replaced. We store it
            // to return it to the caller.
            Tuple<K, V> tuple = null;

            // Try to delete the entry starting from the root page. Here, the root
            // page may be either a Node or a Leaf
            DeleteResult<K, V> result = rootPage.delete( revision, key, value, null, -1 );

            if ( result instanceof NotPresentResult )
            {
                // Key not found.
                return null;
            }

            // Keep the oldRootPage so that we can later access it
            Page<K, V> oldRootPage = rootPage;

            if ( result instanceof RemoveResult )
            {
                // The element was found, and removed
                RemoveResult<K, V> removeResult = ( RemoveResult<K, V> ) result;

                Page<K, V> modifiedPage = removeResult.getModifiedPage();

                // Write the modified page on disk
                // Note that we don't use the holder, the new root page will
                // remain in memory.
                PageHolder<K, V> holder = writePage( modifiedPage, revision );

                // Store the offset on disk in the page in memory
                ( ( AbstractPage<K, V> ) modifiedPage ).setOffset( ( ( PersistedPageHolder<K, V> ) holder )
                    .getOffset() );

                // Store the last offset on disk in the page in memory
                ( ( AbstractPage<K, V> ) modifiedPage )
                    .setLastOffset( ( ( PersistedPageHolder<K, V> ) holder )
                        .getLastOffset() );

                // This is a new root
                rootPage = modifiedPage;
                tuple = removeResult.getRemovedElement();
            }

            // Decrease the number of elements in the current tree if the deletion is successful
            if ( tuple != null )
            {
                btreeHeader.decrementNbElems();

                // We have to update the rootPage on disk
                // Update the BTree header now
                recordManager.updateBtreeHeader( this, ( ( AbstractPage<K, V> ) rootPage ).getOffset() );
            }

            recordManager.addFreePages( this, result.getCopiedPages() );

            // Update the RecordManager header
            recordManager.updateRecordManagerHeader();

            // Store the created rootPage into the revision BTree, this will be stored in RecordManager only if revisions are set to keep
            recordManager.storeRootPage( this, rootPage );

            // Return the value we have found if it was modified
            return tuple;
        }
        finally
        {
            // See above
            writeLock.unlock();
        }
    }


    /**
     * Insert an entry in the BTree.
     * <p>
     * We will replace the value if the provided key already exists in the
     * btree.
     * <p>
     * The revision number is the revision to use to insert the data.
     *
     * @param key Inserted key
     * @param value Inserted value
     * @param revision The revision to use
     * @return an instance of the InsertResult.
     */
    public InsertResult<K, V> insert( K key, V value, long revision ) throws IOException
    {
        if ( key == null )
        {
            throw new IllegalArgumentException( "Key must not be null" );
        }

        // If the key exists, the existing value will be replaced. We store it
        // to return it to the caller.
        V modifiedValue = null;

        // Try to insert the new value in the tree at the right place,
        // starting from the root page. Here, the root page may be either
        // a Node or a Leaf
        InsertResult<K, V> result = rootPage.insert( revision, key, value );

        if ( result instanceof ModifyResult )
        {
            ModifyResult<K, V> modifyResult = ( ( ModifyResult<K, V> ) result );

            Page<K, V> modifiedPage = modifyResult.getModifiedPage();

            // Write the modified page on disk
            // Note that we don't use the holder, the new root page will
            // remain in memory.
            writePage( modifiedPage, revision );

            // The root has just been modified, we haven't split it
            // Get it and make it the current root page
            rootPage = modifiedPage;

            modifiedValue = modifyResult.getModifiedValue();
        }
        else
        {
            // We have split the old root, create a new one containing
            // only the pivotal we got back
            SplitResult<K, V> splitResult = ( ( SplitResult<K, V> ) result );

            K pivot = splitResult.getPivot();
            Page<K, V> leftPage = splitResult.getLeftPage();
            Page<K, V> rightPage = splitResult.getRightPage();
            Page<K, V> newRootPage = null;

            // If the BTree is managed, we have to write the two pages that were created
            // and to keep a track of the two offsets for the upper node
            PageHolder<K, V> holderLeft = writePage( leftPage, revision );

            PageHolder<K, V> holderRight = writePage( rightPage, revision );

            // Create the new rootPage
            newRootPage = new PersistedNode<K, V>( this, revision, pivot, holderLeft, holderRight );

            // If the BTree is managed, we now have to write the page on disk
            // and to add this page to the list of modified pages
            PageHolder<K, V> holder = writePage( newRootPage, revision );

            rootPage = newRootPage;
        }

        // Increase the number of element in the current tree if the insertion is successful
        // and does not replace an element
        if ( modifiedValue == null )
        {
            btreeHeader.incrementNbElems();
        }

        // If the BTree is managed, we have to update the rootPage on disk
        // Update the RecordManager header
        if ( ( writeTransaction == null ) || !writeTransaction.isStarted() )
        {
            recordManager.updateRecordManagerHeader();

            // Update the BTree header now
            recordManager.updateBtreeHeader( this, ( ( AbstractPage<K, V> ) rootPage ).getOffset() );

            // Moved the free pages into the list of free pages
            recordManager.addFreePages( this, result.getCopiedPages() );

            // Store the created rootPage into the revision BTree, this will be stored in RecordManager only if revisions are set to keep
            recordManager.storeRootPage( this, rootPage );
        }

        // Return the value we have found if it was modified
        return result;
    }


    /**
     * Write the data in the ByteBuffer, and eventually on disk if needed.
     *
     * @param channel The channel we want to write to
     * @param bb The ByteBuffer we want to feed
     * @param buffer The data to inject
     * @throws IOException If the write failed
     */
    private void writeBuffer( FileChannel channel, ByteBuffer bb, byte[] buffer ) throws IOException
    {
        int size = buffer.length;
        int pos = 0;

        // Loop until we have written all the data
        do
        {
            if ( bb.remaining() >= size )
            {
                // No flush, as the ByteBuffer is big enough
                bb.put( buffer, pos, size );
                size = 0;
            }
            else
            {
                // Flush the data on disk, reinitialize the ByteBuffer
                int len = bb.remaining();
                size -= len;
                bb.put( buffer, pos, len );
                pos += len;

                bb.flip();

                channel.write( bb );

                bb.clear();
            }
        }
        while ( size > 0 );
    }


    /**
     * Write a page either in the pending pages if the transaction is started,
     * or directly on disk.
     */
    private PageHolder<K, V> writePage( Page<K, V> modifiedPage, long revision ) throws IOException
    {
        if ( ( writeTransaction != null ) && writeTransaction.isStarted() )
        {
            Map<Page<?, ?>, BTree<?, ?>> pendingPages = recordManager.getPendingPages();
            pendingPages.put( modifiedPage, this );

            PageHolder<K, V> pageHolder = new PageHolder<K, V>( this, modifiedPage );

            return pageHolder;
        }
        else
        {
            PageHolder<K, V> pageHolder = recordManager.writePage( this, modifiedPage, revision );

            return pageHolder;
        }
    }

    /**
     * Get the rootPzge associated to a give revision.
     *
     * @param revision The revision we are looking for
     * @return The rootPage associated to this revision
     * @throws IOException If we had an issue while accessing the underlying file
     * @throws KeyNotFoundException If the revision does not exist for this Btree
     */
    public Page<K, V> getRootPage( long revision ) throws IOException, KeyNotFoundException
    {
        return recordManager.getRootPage( this, revision );
    }


    /**
     * Starts a transaction
     */
    public void beginTransaction()
    {
        createTransaction.lock();

        if ( writeTransaction == null )
        {
            writeTransaction = new WriteTransaction( recordManager );
        }

        createTransaction.unlock();

        writeTransaction.start();
    }


    /**
     * Commits a transaction
     */
    public void commit()
    {
        createTransaction.lock();

        if ( writeTransaction == null )
        {
            writeTransaction = new WriteTransaction( recordManager );
        }

        createTransaction.unlock();

        writeTransaction.commit();
    }


    /**
     * Rollback a transaction
     */
    public void rollback()
    {
        createTransaction.lock();

        if ( writeTransaction == null )
        {
            writeTransaction = new WriteTransaction( recordManager );
        }

        createTransaction.unlock();

        writeTransaction.rollback();
    }




    /**
     * @see Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "Managed BTree" );
        sb.append( "[" ).append( btreeHeader.getName() ).append( "]" );
        sb.append( "( pageSize:" ).append( btreeHeader.getPageSize() );

        if ( rootPage != null )
        {
            sb.append( ", nbEntries:" ).append( btreeHeader.getNbElems() );
        }
        else
        {
            sb.append( ", nbEntries:" ).append( 0 );
        }

        sb.append( ", comparator:" );

        if ( keySerializer.getComparator() == null )
        {
            sb.append( "null" );
        }
        else
        {
            sb.append( keySerializer.getComparator().getClass().getSimpleName() );
        }

        sb.append( ", DuplicatesAllowed: " ).append( btreeHeader.isAllowDuplicates() );

        sb.append( ") : \n" );
        sb.append( rootPage.dumpPage( "" ) );

        return sb.toString();
    }
}
