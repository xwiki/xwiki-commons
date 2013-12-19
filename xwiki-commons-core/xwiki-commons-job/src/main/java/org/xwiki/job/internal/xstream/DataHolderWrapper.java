/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.job.internal.xstream;

import java.util.Iterator;

import com.thoughtworks.xstream.converters.DataHolder;

/**
 * Wrap a {@link DataHolder}.
 * 
 * @version $Id$
 * @since 5.4M1
 */
public class DataHolderWrapper implements DataHolder
{
    private final DataHolder wrapped;

    /**
     * @param wrapped the wrapped {@link DataHolder}
     */
    public DataHolderWrapper(DataHolder wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public Object get(Object key)
    {
        return this.wrapped.get(key);
    }

    @Override
    public void put(Object key, Object value)
    {
        this.wrapped.put(key, value);
    }

    @Override
    public Iterator keys()
    {
        return this.wrapped.keys();
    }
}
