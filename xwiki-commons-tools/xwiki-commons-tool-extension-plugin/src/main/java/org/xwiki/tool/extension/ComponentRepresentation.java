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
package org.xwiki.tool.extension;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.internal.RoleHint;
import org.xwiki.stability.Unstable;

/**
 * A class to represent a {@link org.xwiki.component.descriptor.ComponentRole} with a serialized type.
 *
 * @version $Id$
 * @since 12.2RC1
 */
@Unstable
public class ComponentRepresentation
{
    private String role;
    private String type;

    /**
     * @return the role of the component or the default role.
     */
    public String getRole()
    {
        if (StringUtils.isEmpty(this.role)) {
            return RoleHint.DEFAULT_HINT;
        } else {
            return role;
        }
    }

    /**
     * Set the role of the component to retrieve.
     * @param role the role used for this component.
     */
    public void setRole(String role)
    {
        this.role = role;
    }

    /**
     * @return the serialized type of the component. This value is mandatory.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Set the serialized type of the component.
     * @param type a serialized string corresponding to the type of the component.
     */
    public void setType(String type)
    {
        this.type = type;
    }
}
