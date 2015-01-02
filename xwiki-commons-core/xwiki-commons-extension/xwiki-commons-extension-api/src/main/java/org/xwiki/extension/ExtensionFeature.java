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

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.extension.version.VersionConstraint;

import com.google.common.base.Objects;

/**
 * A feature associated to an extension.
 * 
 * @version $Id$
 * @since 7.0M1
 */
public class ExtensionFeature
{
    private String id;

    private VersionConstraint version;

    /**
     * @param id the identifier of this feature
     * @param version the version constraint of this feature
     */
    public ExtensionFeature(String id, VersionConstraint version)
    {
        this.id = id;
        this.version = version;
    }

    /**
     * @param id the identifier of this feature
     */
    public ExtensionFeature(String id)
    {
        this(id, null);
    }

    /**
     * @return the identifier of this feature
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * @return the version constraint of this feature
     */
    public VersionConstraint getVersionConstraint()
    {
        return this.version;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ExtensionFeature) {
            ExtensionFeature otherExtensionFeature = (ExtensionFeature) obj;
            return Objects.equal(otherExtensionFeature.getId(), getId())
                && Objects.equal(otherExtensionFeature.getVersionConstraint(), getVersionConstraint());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(getId());
        builder.append(getVersionConstraint());

        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        builder.append(getId());

        if (getVersionConstraint() != null) {
            builder.append(getVersionConstraint());
        }

        return builder.toString();
    }
}
