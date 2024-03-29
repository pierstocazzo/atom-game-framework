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
package sg.atom.utils.datastructure.tree.mvccbtree.results;


import sg.atom.utils.datastructure.collection.Tuple;
import java.util.List;
import sg.atom.utils.datastructure.tree.mvccbtree.Page;


/**
 * The result of a delete operation, when the child has not been merged, and when
 * we have borrowed an element from the left sibling. It contains the
 * reference to the modified page, and the removed element.
 * 
 * @param <K> The type for the Key
 * @param <V> The type for the stored value

 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BorrowedFromLeftResult<K, V> extends AbstractBorrowedFromSiblingResult<K, V>
{
    /**
     * The default constructor for BorrowedFromLeftResult.
     * 
     * @param modifiedPage The modified page
     * @param modifiedSibling The modified sibling
     * @param removedElement The removed element (can be null if the key wasn't present in the tree)
     */
    public BorrowedFromLeftResult( Page<K, V> modifiedPage, Page<K, V> modifiedSibling,
        Tuple<K, V> removedElement )
    {
        super( modifiedPage, modifiedSibling, removedElement, AbstractBorrowedFromSiblingResult.SiblingPosition.LEFT );
    }


    /**
     * A constructor for BorrowedFromLeftResult which takes a list of copied pages.
     * 
     * @param copiedPages the list of copied pages
     * @param modifiedPage The modified page
     * @param modifiedSibling The modified sibling
     * @param removedElement The removed element (can be null if the key wasn't present in the tree)
     */
    public BorrowedFromLeftResult( List<Page<K, V>> copiedPages, Page<K, V> modifiedPage,
        Page<K, V> modifiedSibling,
        Tuple<K, V> removedElement )
    {
        super( copiedPages, modifiedPage, modifiedSibling, removedElement,
            AbstractBorrowedFromSiblingResult.SiblingPosition.LEFT );
    }


    /**
     * @see Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "Borrowed from left" );
        sb.append( super.toString() );

        return sb.toString();
    }
}
