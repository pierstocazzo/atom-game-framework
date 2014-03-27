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
package sg.atom.utils.datastructure.tree.mvccbtree;

import java.io.IOException;
import sg.atom.utils.datastructure.tree.mvccbtree.exception.EndOfFileExceededException;

/**
 * A Cursor is used to fetch elements in a BTree and is returned by the
 *
 * @see BTree#browse method. The cursor <strng>must</strong> be closed when the
 * user is done with it. <p>
 *
 * @param <V> The type for the stored value
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory
 * Project</a>
 */
public interface ValueCursor<V> extends Cursor<V> {

    /**
     * Find the next key/value
     *
     * @return A Tuple containing the found key and value
     * @throws IOException
     * @throws EndOfFileExceededException
     */
    V next() throws EndOfFileExceededException, IOException;

    /**
     * Find the previous key/value
     *
     * @return A Tuple containing the found key and value
     * @throws IOException
     * @throws EndOfFileExceededException
     */
    V prev() throws EndOfFileExceededException, IOException;

    /**
     * @return The number of elements stored in the cursor
     */
    int size();
}
