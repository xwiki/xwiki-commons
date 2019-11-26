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
package org.xwiki.context.concurrent;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.xwiki.component.annotation.Role;
import org.xwiki.component.manager.ComponentLookupException;

/**
 * Entry point to manipulate all {@link ContextStore}s.
 * 
 * @version $Id$
 * @since 10.10RC1
 */
@Role
public interface ContextStoreManager
{
    /**
     * @return the names of the context entries supported by the various {@link ContextStore} components
     * @throws ComponentLookupException when failing to get {@link ContextStore} components
     */
    Collection<String> getSupportedEntries() throws ComponentLookupException;

    /**
     * Save only the passed context entries in the map.
     * 
     * @param entries the list of context entries to take into account
     * @return the saved context entries
     * @throws ComponentLookupException when failing to get {@link ContextStore} components
     */
    Map<String, Serializable> save(Collection<String> entries) throws ComponentLookupException;

    /**
     * Inject in the current context data found in the passed map.
     * 
     * @param contextStore the stored context data
     * @throws ComponentLookupException when failing to get {@link ContextStore} components
     */
    void restore(Map<String, Serializable> contextStore) throws ComponentLookupException;
}
