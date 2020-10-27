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

import org.xwiki.component.annotation.Role;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.search.AdvancedSearchable;
import org.xwiki.job.JobException;
import org.xwiki.stability.Unstable;

/**
 * A local index of extensions coming from various repositories.
 * 
 * @version $Id$
 * @since 12.10RC1
 */
@Unstable
@Role
public interface ExtensionIndex extends ExtensionRepository, AdvancedSearchable
{
    /**
     * @return the status of the currently running or last indexing process
     */
    ExtensionIndexStatus getStatus();

    /**
     * Start a new indexing process or return the status of the currently running one.
     * 
     * @return the status of the running indexing process
     * @throws JobException when failing to start indexing
     */
    ExtensionIndexStatus index() throws JobException;
}
