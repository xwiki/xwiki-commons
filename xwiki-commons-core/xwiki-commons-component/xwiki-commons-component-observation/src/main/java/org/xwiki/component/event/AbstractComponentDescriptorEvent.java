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
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.component.util.ReflectionUtils;

/**
 * Base class for events about components descriptors.
 *
 * @version $Id$
 * @since 2.6RC2
 */
public abstract class AbstractComponentDescriptorEvent implements ComponentDescriptorEvent
{
    /**
     * Component role.
     */
    private Type roleType;

    /**
     * Component role hint.
     */
    private String roleHint;

    /**
     * Watches all roles (whenever a component is added it'll trigger this event).
     */
    public AbstractComponentDescriptorEvent()
    {
    }

    /**
     * @param role the component role to watch (all components matching this role will trigger this event)
     */
    public AbstractComponentDescriptorEvent(Class<?> role)
    {
        this((Type) role);
    }

    /**
     * @param roleType the component role type to watch (all components matching this role will trigger this event)
     * @since 4.4RC1
     */
    public AbstractComponentDescriptorEvent(Type roleType)
    {
        this.roleType = roleType;
    }

    /**
     * @param role the component role to watch
     * @param roleHint the component rolehint to watch
     */
    public AbstractComponentDescriptorEvent(Class<?> role, String roleHint)
    {
        this((Type) role, roleHint);
    }

    /**
     * @param roleType the component role to watch
     * @param roleHint the component rolehint to watch
     * @since 4.4RC1
     */
    public AbstractComponentDescriptorEvent(Type roleType, String roleHint)
    {
        this.roleType = roleType;
        this.roleHint = roleHint;
    }

    @Override
    public Class<?> getRole()
    {
        return ReflectionUtils.getTypeClass(getRoleType());
    }

    @Override
    public Type getRoleType()
    {
        return this.roleType;
    }

    @Override
    public String getRoleHint()
    {
        return this.roleHint;
    }

    @Override
    public boolean matches(Object otherEvent)
    {
        boolean result = false;

        if (otherEvent instanceof AbstractComponentDescriptorEvent) {
            // If we're watching all roles return a match
            if (getRole() == null) {
                result = true;
            } else {
                ComponentDescriptorEvent event = (ComponentDescriptorEvent) otherEvent;

                if (getRoleType().equals(event.getRoleType())) {
                    result = getRoleHint() == null || getRoleHint().equals(event.getRoleHint());
                }
            }
        }

        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }

        if (obj != null && obj.getClass() == getClass()) {
            return Objects.equals(getRoleType(), ((ComponentDescriptorEvent) obj).getRoleType())
                && StringUtils.equals(getRoleHint(), ((ComponentDescriptorEvent) obj).getRoleHint());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(getRoleType());
        builder.append(getRoleHint());

        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        return getRoleType() + ":" + getRoleHint();
    }
}
