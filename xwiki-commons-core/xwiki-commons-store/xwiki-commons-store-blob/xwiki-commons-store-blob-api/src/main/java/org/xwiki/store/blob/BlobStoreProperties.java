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

/**
 * Marker + minimal contract for typed blob store properties beans, used by factories and stores.
 * Implementations are regular Java beans annotated with XWiki {@code @Property*} annotations
 * and JSR-303 constraints. Values are populated using the BeanManager.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Unstable
public interface BlobStoreProperties
{
    /**
     * @return the unique store name
     */
    String getName();

    /**
     * @return the store type (factory hint)
     */
    String getType();
}

