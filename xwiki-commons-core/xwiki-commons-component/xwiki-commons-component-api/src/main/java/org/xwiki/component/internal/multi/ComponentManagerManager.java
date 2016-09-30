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
package org.xwiki.component.internal.multi;

import org.xwiki.component.annotation.Role;
import org.xwiki.component.manager.ComponentManager;

/**
 * Provide {@link ComponentManager} associated to keys.
 * <p>
 * The provided id is generally of the form <code>prefix:subid</code> where <code>prefix</code> is used to find the
 * proper factory. <code>:</code> can be escaped using <code>\</code> and <code>\</code> need to be escaped as well.
 *
 * @version $Id$
 * @since 3.3M2
 */
@Role
public interface ComponentManagerManager
{
    /**
     * @param namespace the identifier of the {@link ComponentManager} to return or create
     * @param create if true the {@link ComponentManager} is created if it does not exists
     * @return a {@link ComponentManager} instance or null if none exists associated to the provided id and
     *         <code>create</code> is false
     */
    ComponentManager getComponentManager(String namespace, boolean create);
}
