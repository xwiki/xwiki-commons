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
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.text.XWikiToStringBuilder;

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
        this(componentRole.getRoleType(), componentRole.getRoleHint());
    }

    /**
     * @param roleType the role type
     * @param roleHint the role hint
     * @since 10.10RC1
     */
    public DefaultComponentRole(Type roleType, String roleHint)
    {
        setRoleType(roleType);
        setRoleHint(roleHint);
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
        return this.roleHint;
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new XWikiToStringBuilder(this);
        builder.append("role", null == getRoleType() ? "<null>" : getRoleType());
        builder.append("hint", getRoleHint());
        return builder.toString();
    }

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
                // object must be ComponentRole at this point
                ComponentRole<?> cr = (ComponentRole<?>) object;
                result =
                    Objects.equals(getRoleType(), cr.getRoleType()) && Objects.equals(getRoleHint(), cr.getRoleHint());
            }
        }

        return result;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(getRoleType());
        builder.append(getRoleHint());

        return builder.toHashCode();
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
