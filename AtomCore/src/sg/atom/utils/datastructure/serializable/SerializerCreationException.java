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
package sg.atom.utils.datastructure.serializable;


/**
 * An exception thrown when we can't create a serializer
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SerializerCreationException extends RuntimeException
{
    /** The serial version UUID */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new instance of SerializerCreationException.
     */
    public SerializerCreationException()
    {
    }


    /**
     * Creates a new instance of SerializerCreationException.
     *
     * @param explanation The message associated with the exception
     */
    public SerializerCreationException( String explanation )
    {
        super( explanation );
    }


    /**
     * Creates a new instance of SerializerCreationException.
     */
    public SerializerCreationException( Throwable cause )
    {
        super( cause );
    }


    /**
     * Creates a new instance of SerializerCreationException.
     *
     * @param explanation The message associated with the exception
     * @param cause The root cause for this exception
     */
    public SerializerCreationException( String explanation, Throwable cause )
    {
        super( explanation, cause );
    }
}
