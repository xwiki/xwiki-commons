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
import org.xwiki.store.blob.internal.BlobOptionSupport;

/**
 * Defines how write operations should behave when the target blob already exists.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Unstable
public enum BlobWriteMode implements BlobOption
{
    /**
     * Replace the target blob if it exists.
     */
    REPLACE_EXISTING("Replace existing blob"),

    /**
     * Create a new blob and fail if the target already exists.
     */
    CREATE_NEW("Create new blob only if absent");

    private final String description;

    BlobWriteMode(String description)
    {
        this.description = description;
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Resolves the write mode from the given options, returning the default value if no write mode is specified.
     *
     * @param defaultMode the default write mode to use if none is specified in the options
     * @param options the options to search for a write mode
     * @return the resolved write mode, or the default if none is found
     * @throws IllegalArgumentException if multiple write modes are specified
     */
    public static BlobWriteMode resolve(BlobWriteMode defaultMode, BlobOption... options)
    {
        BlobWriteMode explicitMode = BlobOptionSupport.findSingleOption(BlobWriteMode.class, options);
        return explicitMode != null ? explicitMode : defaultMode;
    }
}
