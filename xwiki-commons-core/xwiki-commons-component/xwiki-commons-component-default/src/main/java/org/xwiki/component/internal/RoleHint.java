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
package org.xwiki.component.internal;

import java.lang.reflect.Type;

import org.xwiki.component.util.ReflectionUtils;

/**
 * Represent the unique identifier of a Component (pair Role/Hint).
 * 
 * @param <T> the role class
 * @version $Id$
 * @since 2.0M1
 */
public class RoleHint<T>
{
    /**
     * @see #getRole()
     */
    private Type role;

    /**
     * @see #getHint()
     */
    private String hint;

    /**
     * @param role the component role
     * @since 4.0M1
     */
    public RoleHint(Type role)
    {
        this(role, null);
    }

    /**
     * @param role the component role
     * @param hint the component hint
     * @since 4.0M1
     */
    public RoleHint(Type role, String hint)
    {
        this.role = role;
        this.hint = hint;
        if (this.hint == null) {
            this.hint = "default";
        }
    }

    /**
     * @return the component role
     * @since 4.0M1
     */
    public Type getRoleType()
    {
        return this.role;
    }

    /**
     * @return the component role as class
     * @since 4.0M1
     */
    public Class<T> getRoleClass()
    {
        return ReflectionUtils.getTypeClass(this.role);
    }

    /**
     * @return the component hint
     */
    public String getHint()
    {
        return this.hint;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }

        RoleHint<T> rolehint = (RoleHint<T>) obj;

        // It's possible Class reference are not the same when it coming for different ClassLoader so we compare class
        // names
        return getRoleType() == rolehint.getRoleType()
            && (getHint() == rolehint.getHint() || (getHint() != null && getHint().equals(rolehint.getHint())));
    }

    @Override
    public int hashCode()
    {
        int hash = 8;
        hash = 31 * hash + (null == getRoleType() ? 0 : getRoleType().hashCode());
        hash = 31 * hash + (null == getHint() ? 0 : getHint().hashCode());

        return hash;
    }

    @Override
    public String toString()
    {
        return "role = [" + getRoleType() + "] hint = [" + getHint() + "]";
    }
}
