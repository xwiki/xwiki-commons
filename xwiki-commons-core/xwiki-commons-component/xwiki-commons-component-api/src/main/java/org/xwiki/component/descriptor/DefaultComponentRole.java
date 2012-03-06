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

import java.lang.reflect.Type;

import org.xwiki.component.util.ObjectUtils;
import org.xwiki.component.util.ReflectionUtils;

/**
 * @param <T> the type of the component role
 * @version $Id$
 */
public class DefaultComponentRole<T> implements ComponentRole<T>
{
    /**
     * @see #getRole()
     */
    private Type roleType;

    /**
     * @see #getRoleHint()
     */
    private String roleHint = "default";

    /**
     * Default constructor.
     */
    public DefaultComponentRole()
    {
    }

    /**
     * Create a new {@link DefaultComponentRole} by cloning the provided {@link ComponentRole}.
     * 
     * @param componentRole the component role to clone
     * @since 3.4M1
     */
    public DefaultComponentRole(ComponentRole<T> componentRole)
    {
        setRoleType(componentRole.getRoleType());
        setRoleHint(componentRole.getRoleHint());
    }

    @Override
    public Type getRoleType()
    {
        return this.roleType;
    }

    /**
     * @param roleType the type of the role
     */
    public void setRoleType(Type roleType)
    {
        this.roleType = roleType;
    }

    /**
     * @param roleHint the hint of the component role
     */
    public void setRoleHint(String roleHint)
    {
        this.roleHint = roleHint;
    }

    @Override
    public String getRoleHint()
    {
        return roleHint;
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder();

        buffer.append("role = [").append(null == getRoleType() ? "<null>" : getRoleType()).append("]");
        buffer.append(" hint = [").append(getRoleHint()).append("]");

        return buffer.toString();
    }

    /**
     * {@inheritDoc}
     * 
     * @since 3.3M1
     */
    @Override
    public boolean equals(Object object)
    {
        boolean result;

        if (this == object) {
            result = true;
        } else {
            if (object == null || object.getClass() != getClass()) {
                result = false;
            } else {
                // object must be Syntax at this point
                ComponentRole cr = (ComponentRole) object;
                result =
                    ObjectUtils.equals(getRoleType(), cr.getRoleType())
                        && ObjectUtils.equals(getRoleHint(), cr.getRoleHint());
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @since 3.3M1
     */
    @Override
    public int hashCode()
    {
        int hash = 7;

        hash = 31 * hash + ObjectUtils.hasCode(getRoleType());
        hash = 31 * hash + ObjectUtils.hasCode(getRoleHint());

        return hash;
    }

    // deprecated

    /**
     * @param role the class of the component role
     * @deprecated since 4.0M1 use {@link #setRoleType(Type)} instead
     */
    @Deprecated
    public void setRole(Class<T> role)
    {
        this.roleType = role;
    }

    @Override
    @Deprecated
    public Class<T> getRole()
    {
        return ReflectionUtils.getTypeClass(getRoleType());
    }
}
