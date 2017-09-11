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
package org.xwiki.component.event;

import java.lang.reflect.Type;

/**
 * Event sent to tell that a new Component Descriptor has been unregistered.
 * <p>
 * The event also send the following parameters:
 * </p>
 * <ul>
 * <li>source: the {@link org.xwiki.component.manager.ComponentManager} where the component was registered</li>
 * <li>data: the {@link org.xwiki.component.descriptor.ComponentDescriptor} instance</li>
 * </ul>
 *
 * @version $Id$
 * @since 2.6RC2
 */
public class ComponentDescriptorRemovedEvent extends AbstractComponentDescriptorEvent
{
    /**
     * Watches all roles (whenever a component is added it'll trigger this event).
     */
    public ComponentDescriptorRemovedEvent()
    {

    }

    /**
     * @param role the component role to watch (all components matching this role will trigger this event)
     */
    public ComponentDescriptorRemovedEvent(Class<?> role)
    {
        super(role);
    }

    /**
     * @param roleType the component role to watch (all components matching this role will trigger this event)
     * @since 4.4RC1
     */
    public ComponentDescriptorRemovedEvent(Type roleType)
    {
        super(roleType);
    }

    /**
     * @param role the component role to watch
     * @param roleHint the component role hint to watch
     */
    public ComponentDescriptorRemovedEvent(Class<?> role, String roleHint)
    {
        super(role, roleHint);
    }

    /**
     * @param roleType the component role to watch
     * @param roleHint the component role hint to watch
     * @since 4.4RC1
     */
    public ComponentDescriptorRemovedEvent(Type roleType, String roleHint)
    {
        super(roleType, roleHint);
    }

    @Override
    public boolean matches(Object otherEvent)
    {
        boolean result = false;

        if (ComponentDescriptorRemovedEvent.class.isAssignableFrom(otherEvent.getClass())) {
            result = super.matches(otherEvent);
        }

        return result;
    }
}
