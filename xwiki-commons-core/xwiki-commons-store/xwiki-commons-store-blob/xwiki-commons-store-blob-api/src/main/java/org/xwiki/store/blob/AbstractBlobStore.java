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
package org.xwiki.store.blob;

import org.xwiki.stability.Unstable;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * Abstract base class for {@link BlobStore} implementations.
 *
 * @param <T> the typed properties bean for this store
 * @version $Id$
 * @since 17.10.0RC1
 */
@Unstable
public abstract class AbstractBlobStore<T extends BlobStoreProperties> implements BlobStore
{
    protected String name;

    protected String hint;

    protected T properties;

    /**
     * Initialize this blob store with the given name, hint, and properties.
     * Must be called before performing any other operations.
     *
     * @param name the name of this blob store
     * @param hint the hint of this blob store implementation
     * @param properties the typed properties for this blob store
     */
    protected void initialize(String name, String hint, T properties)
    {
        this.name = name;
        this.hint = hint;
        this.properties = properties;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public String getHint()
    {
        return this.hint;
    }

    @Override
    public T getProperties()
    {
        return this.properties;
    }

    @Override
    public String toString()
    {
        return new XWikiToStringBuilder(this)
            .append("name", this.name)
            .append("hint", this.hint)
            .toString();
    }
}
