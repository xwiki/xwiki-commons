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
package org.xwiki.component.descriptor;

import java.util.Collection;

/**
 * Represent a component.
 *
 * @version $Id$
 * @param <T> the type of the component role
 * @since 1.7M1
 */
public interface ComponentDescriptor<T> extends ComponentRole<T>
{
    /**
     * Default priority value for {@link #getRoleTypePriority()} and {@link #getRoleHintPriority()}.
     * 
     * @since 15.4RC1
     */
    int DEFAULT_PRIORITY = 1000;

    /**
     * @return the class of the component implementation
     */
    Class<? extends T> getImplementation();

    /**
     * @return the way the component should be instantiated
     * @see ComponentInstantiationStrategy
     */
    ComponentInstantiationStrategy getInstantiationStrategy();

    /**
     * @return the components on which this component depends
     */
    Collection<ComponentDependency<?>> getComponentDependencies();

    /**
     * @return true if failing to initialize the component should fail APIs like
     *         {@link org.xwiki.component.manager.ComponentManager#getInstanceList(java.lang.reflect.Type)}.
     * @since 15.0RC1
     */
    default boolean isMandatory()
    {
        return false;
    }

    /**
     * The role type priority represents the priority for ordering components sharing the same type: it can be used to
     * order the components when retrieving a list of components of the same type. The lower the value, the higher the
     * priority.
     *
     * @return the role type priority of the component.
     * @since 15.4RC1
     */
    default int getRoleTypePriority()
    {
        return DEFAULT_PRIORITY;
    }

    /**
     * The role hint priority represents the priority for ordering components sharing the same type and hint: it can be
     * used to decide which component should be overridden when loading them. The lower the value, the higher the
     * priority.
     *
     * @return the role hint priority of the component.
     * @since 15.4RC1
     */
    default int getRoleHintPriority()
    {
        return DEFAULT_PRIORITY;
    }
}
