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
package org.xwiki.extension.repository;

import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ResolveException;

/**
 * Base class for {@link ExtensionRepository} implementations.
 *
 * @version $Id$
 * @since 4.0M1
 */
public abstract class AbstractExtensionRepository implements ExtensionRepository
{
    /**
     * @see #getDescriptor()
     */
    private ExtensionRepositoryDescriptor descriptor;

    /**
     * Default constructor. Used by extended classes which can't set the id in there constructor but make sure it's set
     * later or that {@link #getId()} is overwritten.
     */
    protected AbstractExtensionRepository()
    {

    }

    /**
     * @param descriptor the repository descriptor
     * @since 4.3M1
     */
    protected AbstractExtensionRepository(ExtensionRepositoryDescriptor descriptor)
    {
        setDescriptor(descriptor);
    }

    /**
     * @param id the repository identifier
     * @deprecated since 4.3M1 use {@link #AbstractExtensionRepository(ExtensionRepositoryDescriptor)} instead
     */
    @Deprecated
    protected AbstractExtensionRepository(ExtensionRepositoryId id)
    {
        setId(new ExtensionRepositoryId(id));
    }

    /**
     * @param descriptor the repository descriptor
     * @since 4.3M1
     */
    protected void setDescriptor(ExtensionRepositoryDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    /**
     * @param id the repository identifier
     * @deprecated since 4.3M1 use {@link #setDescriptor(ExtensionRepositoryDescriptor)} instead
     */
    @Deprecated
    protected void setId(ExtensionRepositoryId id)
    {
        this.descriptor = id;
    }

    // ExtensionRepository

    @Override
    public ExtensionRepositoryDescriptor getDescriptor()
    {
        return this.descriptor;
    }

    @Override
    public ExtensionRepositoryId getId()
    {
        return new ExtensionRepositoryId(this.descriptor);
    }

    @Override
    public boolean exists(ExtensionId extensionId)
    {
        boolean exists;

        try {
            resolve(extensionId);
            exists = true;
        } catch (ResolveException e) {
            exists = false;
        }

        return exists;
    }
}
