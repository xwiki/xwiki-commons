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
package org.xwiki.component.manager;

import java.util.List;
import java.util.Map;

import org.xwiki.component.manager.ComponentLookupException;

public interface CompatibilityComponentManager
{
    /**
     * Find a component instance that implements that passed interface class. If the component has a singleton lifecycle
     * then this method always return the same instance.
     *
     * @param <T> the component role type
     * @param role the class (aka role) that the component implements
     * @return the component instance
     * @throws ComponentLookupException in case the component cannot be found
     * @deprecated since 4.0M1 use {@link #getInstance(java.lang.reflect.Type)} instead
     */
    @Deprecated
    <T> T lookup(Class<T> role) throws ComponentLookupException;

    /**
     * Find a component instance that implements that passed interface class. If the component has a singleton lifecycle
     * then this method always return the same instance.
     *
     * @param <T> the component role type
     * @param role the class (aka role) that the component implements
     * @param hint the hint that differentiates a component implementation from another one (each component is
     *            registered with a hint; the "default" hint being the default)
     * @return the component instance
     * @throws ComponentLookupException in case the component cannot be found
     * @deprecated since 4.0M1 use {@link #getInstance(java.lang.reflect.Type, String)} instead
     */
    @Deprecated
    <T> T lookup(Class<T> role, String hint) throws ComponentLookupException;

    /**
     * Find all the components implementing the provided role.
     *
     * @param role the class of the components role
     * @return the components
     * @param <T> the type of the components role
     * @throws ComponentLookupException if any error happen during component search
     * @deprecated since 4.0M1 use {@link #getInstanceList(java.lang.reflect.Type)} instead
     */
    @Deprecated
    <T> List<T> lookupList(Class<T> role) throws ComponentLookupException;

    /**
     * Find all the components implementing the provided role and organize then in a {@link java.util.Map} with role hint as key.
     *
     * @param role the class of the components role
     * @return the components
     * @param <T> the type of the components role
     * @throws ComponentLookupException if any error happen during component search
     * @deprecated since 4.0M1 use {@link #getInstanceMap(java.lang.reflect.Type)} instead
     */
    @Deprecated
    <T> Map<String, T> lookupMap(Class<T> role) throws ComponentLookupException;
}
