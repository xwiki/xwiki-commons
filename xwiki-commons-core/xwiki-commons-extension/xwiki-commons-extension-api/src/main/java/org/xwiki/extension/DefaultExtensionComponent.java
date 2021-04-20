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
package org.xwiki.extension;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Default implementation of {@link ExtensionComponent}.
 *
 * @version $Id$
 * @since 13.3RC1
 */
public class DefaultExtensionComponent implements ExtensionComponent
{
    private static final Pattern WHITE_SPACES_PATTEN = Pattern.compile("\\s+");

    private final String roleType;

    private final String roleHint;

    /**
     * @param roleType the class of the component role as String
     * @param roleHint the hint of the component role
     */
    public DefaultExtensionComponent(String roleType, String roleHint)
    {
        // Remove white spaces from the type to make sure the same type is always giving the same String
        this.roleType = toCanonicalComponentType(roleType);
        this.roleHint = toCanonicalComponentHint(roleHint);
    }

    /**
     * @param componentType the component type to clean
     * @return the canonical version of the component type name
     */
    public static String toCanonicalComponentType(String componentType)
    {
        return componentType != null ? WHITE_SPACES_PATTEN.matcher(componentType).replaceAll("") : null;
    }

    /**
     * @param componentHint the component hint to clean
     * @return the canonical version of the component hint
     * @since 13.3
     */
    public static String toCanonicalComponentHint(String componentHint)
    {
        return StringUtils.isEmpty(componentHint) ? "default" : componentHint;
    }

    @Override
    public String getRoleType()
    {
        return this.roleType;
    }

    @Override
    public String getRoleHint()
    {
        return this.roleHint;
    }

    @Override
    public boolean equalRoleType(String type)
    {
        return getRoleType().equals(toCanonicalComponentType(type));
    }

    // Object

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ExtensionComponent) {
            ExtensionComponent otherAuthor = (ExtensionComponent) obj;

            return StringUtils.equals(getRoleType(), otherAuthor.getRoleType())
                && StringUtils.equals(getRoleHint(), otherAuthor.getRoleHint());
        } else {
            return false;
        }
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
        StringBuilder builder = new StringBuilder();

        builder.append(getRoleType());
        builder.append('(');
        builder.append(getRoleHint());
        builder.append(')');

        return builder.toString();
    }
}
