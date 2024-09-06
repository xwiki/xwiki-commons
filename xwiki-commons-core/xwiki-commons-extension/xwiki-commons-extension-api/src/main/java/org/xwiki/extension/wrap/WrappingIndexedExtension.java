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
package org.xwiki.extension.wrap;

import org.xwiki.extension.Extension;
import org.xwiki.extension.index.IndexedExtension;

/**
 * Wrap an indexed extension.
 *
 * @param <T> the extension type
 * @version $Id$
 * @since 12.10
 */
public class WrappingIndexedExtension<T extends Extension> extends WrappingRatingExtension<T>
    implements IndexedExtension
{
    /**
     * @param extension the wrapped extension
     */
    public WrappingIndexedExtension(T extension)
    {
        super(extension);
    }

    /**
     * A default constructor allowing to set the wrapped object later.
     * 
     * @since 16.8.0RC1
     */
    protected WrappingIndexedExtension()
    {

    }

    // IndexedExtension

    @Override
    @javax.annotation.Nullable
    public Boolean isCompatible(String namespace)
    {
        if (getWrapped() instanceof IndexedExtension indexedExtension) {
            return indexedExtension.isCompatible(namespace);
        }

        return null;
    }
}
