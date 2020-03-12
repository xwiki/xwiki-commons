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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Base class for {@link ExtensionPattern} implementations.
 *
 * @version $Id$
 * @since 12.2CR1
 */
public abstract class AbstractExtensionPattern implements ExtensionPattern
{
    /**
     * @see #getIdPattern()
     */
    protected final Pattern idPattern;

    /**
     * Create new instance by cloning the provided one.
     *
     * @param pattern a pattern to copy
     */
    public AbstractExtensionPattern(ExtensionPattern pattern)
    {
        this(pattern.getIdPattern());
    }

    /**
     * Create new instance by cloning the provided one with different version constraint.
     *
     * @param idPattern a regular expression matching all the ids to exclude
     */
    public AbstractExtensionPattern(Pattern idPattern)
    {
        this.idPattern = idPattern;
    }

    /**
     * @param id the exact id to exclude
     */
    public AbstractExtensionPattern(String id)
    {
        this(Pattern.compile(id, Pattern.LITERAL));
    }

    @Override
    public Pattern getIdPattern()
    {
        return this.idPattern;
    }

    /**
     * @param extensionId the extension id to match
     * @return true if the passed extension is matched by the pattern
     */
    public boolean matches(String extensionId)
    {
        return (getIdPattern() == null || getIdPattern().matcher(extensionId).matches());
    }

    @Override
    public boolean matches(ExtensionId extensionId)
    {
        return matches(extensionId.getId());
    }

    @Override
    public boolean matches(ExtensionDependency dependency)
    {
        return matches(dependency.getId());
    }

    // Object

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();

        str.append(getIdPattern());

        return str.toString();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(getIdPattern() != null ? getIdPattern().pattern() : null);

        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        boolean equals;

        if (obj instanceof ExtensionPattern) {
            ExtensionPattern otherPattern = (ExtensionPattern) obj;

            EqualsBuilder builder = new EqualsBuilder();

            builder.append(getIdPattern() != null ? getIdPattern().pattern() : null,
                otherPattern.getIdPattern() != null ? otherPattern.getIdPattern().pattern() : null);

            equals = builder.isEquals();
        } else {
            equals = false;
        }

        return equals;
    }
}
