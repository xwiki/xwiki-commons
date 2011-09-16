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

/**
 * @param <T> the type of the component role
 * @version $Id$
 */
public class DefaultComponentRole<T> implements ComponentRole<T>
{
    /**
     * @see #getRole()
     */
    private Class<T> role;

    /**
     * @see #getRoleHint()
     */
    private String roleHint = "default";

    /**
     * @param role the class of the component role
     */
    public void setRole(Class<T> role)
    {
        this.role = role;
    }

    @Override
    public Class<T> getRole()
    {
        return this.role;
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
        StringBuffer buffer = new StringBuffer();
        buffer.append("role = [").append(getRole().getName()).append("]");
        buffer.append(" hint = [").append(getRoleHint()).append("]");
        return buffer.toString();
    }
}
