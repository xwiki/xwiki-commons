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
     * Default constructor.
     */
    public DefaultComponentRole()
    {
    }

    /**
     * Create a new {@link DefaultComponentRole} by cloning the provided {@link ComponentRole}.
     * 
     * @param componentRole the component role to clone
     * @since 3.3
     */
    public DefaultComponentRole(ComponentRole<T> componentRole)
    {
        setRole(componentRole.getRole());
        setRoleHint(componentRole.getRoleHint());
    }

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
        buffer.append("role = [").append(null == getRole() ? "<null>" : getRole().getName()).append("]");
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

        // See http://www.technofundo.com/tech/java/equalhash.html for the detail of this algorithm.
        if (this == object) {
            result = true;
        } else {
            if ((object == null) || (object.getClass() != this.getClass())) {
                result = false;
            } else {
                // object must be Syntax at this point
                DefaultComponentRole cr = (DefaultComponentRole) object;
                result =
                    (getRole() == cr.getRole() || (getRole() != null && getRole().equals(cr.getRole())))
                        && (getRoleHint() == cr.getRoleHint() || (getRoleHint() != null && getRoleHint().equals(
                            cr.getRoleHint())));
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
        // Random number. See http://www.technofundo.com/tech/java/equalhash.html for the detail of this
        // algorithm.
        int hash = 7;
        hash = 31 * hash + (null == getRole() ? 0 : getRole().hashCode());
        hash = 31 * hash + (null == getRoleHint() ? 0 : getRoleHint().hashCode());
        return hash;
    }
}
