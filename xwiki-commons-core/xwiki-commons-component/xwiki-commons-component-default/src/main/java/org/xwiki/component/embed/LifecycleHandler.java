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
package org.xwiki.component.embed;

import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;

/**
 * A lifecycle handler is used when instantiating a Component and can perform operation to set up the component (for
 * example if a component implements a given interface calls a method of that interface, etc).
 *
 * @version $Id$
 * @since 3.2RC1
 */
public interface LifecycleHandler
{
    /**
     * Handle initialization of the passed component instance.
     *
     * @param instance the component instance to initialize
     * @param descriptor the descriptor of the passed component
     * @param componentManager the component manager which is initializing the Component
     * @param <T> the type of the Component
     * @throws Exception in case the handler fails to handle the Component initialization
     */
    <T> void handle(T instance, ComponentDescriptor<T> descriptor, ComponentManager componentManager)
        throws Exception;
}
