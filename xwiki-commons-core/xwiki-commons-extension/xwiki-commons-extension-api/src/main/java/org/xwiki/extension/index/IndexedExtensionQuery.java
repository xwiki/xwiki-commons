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
package org.xwiki.extension.index;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.extension.repository.search.ExtensionQuery;
import org.xwiki.stability.Unstable;

/**
 * Extends {@link ExtensionQuery} with extension index specific criteria.
 * 
 * @version $Id$
 * @since 12.7RC1
 */
@Unstable
public class IndexedExtensionQuery extends ExtensionQuery
{
    private final List<String> repositories = new ArrayList<>();

    private Boolean compatible;

    /**
     * @param repositoryId the repository from which the extension is coming from
     * @return this
     */
    public IndexedExtensionQuery fromRepository(String repositoryId)
    {
        this.repositories.add(repositoryId);

        return this;
    }

    /**
     * @param compatible true if only compatible extension should be returned
     * @return this
     */
    public IndexedExtensionQuery setCompatible(boolean compatible)
    {
        this.compatible = compatible;

        return this;
    }

    /**
     * @return true if only compatible extension should be returned
     */
    public Boolean getCompatible()
    {
        return this.compatible;
    }
}
