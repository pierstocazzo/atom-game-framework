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


import java.io.IOException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import sg.atom.utils.datastructure.tree.mvccbtree.AbstractPage;
import sg.atom.utils.datastructure.tree.mvccbtree.BTree;
import sg.atom.utils.datastructure.tree.mvccbtree.Page;
import sg.atom.utils.datastructure.tree.mvccbtree.PageHolder;
import sg.atom.utils.datastructure.tree.mvccbtree.RecordManager;

import sg.atom.utils.datastructure.tree.mvccbtree.exception.BTreeOperationException;
import sg.atom.utils.datastructure.tree.mvccbtree.exception.EndOfFileExceededException;


/**
 * A Value holder. As we may not store all the values in memory (except for an in-memory
 * BTree), we will use a SoftReference to keep a reference to a Value, and if it's null,
 * then we will load the Value from the underlying physical support, using the offset.
 *
 * @param <E> The type for the stored element (either a value or a page)
 * @param <K> The type of the BTree key
 * @param <V> The type of the BTree value
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PersistedPageHolder<K, V> extends PageHolder<K, V>
{
    /** The RecordManager */
    private RecordManager recordManager;

    /** The cache */
    private Cache cache;

    /** The offset of the first {@link PageIO} storing the page on disk */
    private long offset;

    /** The offset of the last {@link PageIO} storing the page on disk */
    private long lastOffset;


    /**
     * Create a new holder storing an offset and a SoftReference containing the element.
     *
     * @param page The element to store into a SoftReference
     */
    public PersistedPageHolder( BTree<K, V> btree, Page<K, V> page )
    {
        super( btree, page );
        cache = ( ( PersistedBTree<K, V> ) btree ).getCache();
        recordManager = ( ( PersistedBTree<K, V> ) btree ).getRecordManager();
        offset = ( ( AbstractPage<K, V> ) page ).getOffset();
        lastOffset = ( ( AbstractPage<K, V> ) page ).getLastOffset();

        ( ( AbstractPage<K, V> ) page ).setOffset( offset );
        ( ( AbstractPage<K, V> ) page ).setLastOffset( lastOffset );

        cache.put( new Element( offset, page ) );
    }


    /**
     * Create a new holder storing an offset and a SoftReference containing the element.
     *
     * @param page The element to store into a SoftReference
     */
    public PersistedPageHolder( BTree<K, V> btree, Page<K, V> page, long offset, long lastOffset )
    {
        super( btree, page );
        cache = ( ( PersistedBTree<K, V> ) btree ).getCache();
        recordManager = ( ( PersistedBTree<K, V> ) btree ).getRecordManager();
        this.offset = offset;
        this.lastOffset = lastOffset;

        if ( page != null )
        {
            ( ( AbstractPage<K, V> ) page ).setOffset( offset );
            ( ( AbstractPage<K, V> ) page ).setLastOffset( lastOffset );
        }

        cache.put( new Element( offset, page ) );
    }


    /**
     * {@inheritDoc}
     * @throws IOException
     * @throws EndOfFileExceededException
     */
    public Page<K, V> getValue()
    {
        Element element = cache.get( offset );

        if ( element == null )
        {
            // We haven't found the element in the cache, reload it
            // We have to fetch the element from disk, using the offset now
            Page<K, V> page = fetchElement();

            cache.put( new Element( offset, page ) );

            return page;
        }

        Page<K, V> page = ( Page<K, V> ) element.getObjectValue();

        if ( page == null )
        {
            // We have to fetch the element from disk, using the offset now
            page = fetchElement();

            ( ( AbstractPage<K, V> ) page ).setOffset( offset );
            ( ( AbstractPage<K, V> ) page ).setLastOffset( lastOffset );

            cache.put( new Element( offset, page ) );
        }

        return page;
    }


    /**
     * Retrieve the value from the disk, using the BTree and offset
     * @return The deserialized element (
     */
    private Page<K, V> fetchElement()
    {
        try
        {
            Page<K, V> element = recordManager.deserialize( btree, offset );

            return element;
        }
        catch ( EndOfFileExceededException eofee )
        {
            throw new BTreeOperationException( eofee.getMessage() );
        }
        catch ( IOException ioe )
        {
            throw new BTreeOperationException( ioe.getMessage() );
        }
    }


    /**
     * @return The offset of the first {@link PageIO} storing the data on disk
     */
    public long getOffset()
    {
        return offset;
    }


    /**
     * @return The offset of the last {@link PageIO} storing the data on disk
     */
    public long getLastOffset()
    {
        return lastOffset;
    }


    /**
     * @see Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        Page<K, V> page = getValue();

        if ( page != null )
        {
            sb.append( btree.getName() ).append( "[" ).append( offset ).append( ", " ).append( lastOffset )
                .append( "]:" ).append( page );
        }
        else
        {
            sb.append( btree.getName() ).append( "[" ).append( offset ).append( ", " ).append( lastOffset )
                .append( "]" );
        }

        return sb.toString();
    }
}
