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

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.extension.internal.converter.ExtensionIdConverter;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.internal.DefaultVersion;

import com.google.common.base.Objects;

/**
 * The combination of properties which makes an extension unique.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class ExtensionId implements Serializable, Comparable<ExtensionId>
{
    /**
     * Type instance for {@code List<ExtensionId>}.
     *
     * @since 8.0M1
     */
    public static final ParameterizedType TYPE_LIST = new DefaultParameterizedType(null, List.class, ExtensionId.class);

    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see #getId()
     */
    private final String id;

    /**
     * @see #getVersion()
     */
    private final Version version;

    private transient int hashCode = -1;

    /**
     * @param id the extension identifier
     * @since 8.0M1
     * @since 7.4.6
     */
    public ExtensionId(String id)
    {
        this(id, (Version) null);
    }

    /**
     * @param id the extension identifier
     * @param version the extension version
     */
    public ExtensionId(String id, String version)
    {
        this(id, version != null ? new DefaultVersion(version) : null);
    }

    /**
     * @param id the extension identifier
     * @param version the extension version
     */
    public ExtensionId(String id, Version version)
    {
        this.id = id;
        this.version = version;
    }

    /**
     * @return the extension identifier
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * @return the extension version
     */
    public Version getVersion()
    {
        return this.version;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ExtensionId) {
            ExtensionId extensionId = (ExtensionId) obj;

            return Objects.equal(extensionId.getId(), getId()) && Objects.equal(extensionId.getVersion(), getVersion());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        if (this.hashCode == -1) {
            HashCodeBuilder builder = new HashCodeBuilder();

            builder.append(getId());
            builder.append(getVersion());

            this.hashCode = builder.toHashCode();
        }

        return this.hashCode;
    }

    @Override
    public String toString()
    {
        return ExtensionIdConverter.toString(this);
    }

    @Override
    public int compareTo(ExtensionId o)
    {
        if (o == null) {
            return -1;
        }

        CompareToBuilder builder = new CompareToBuilder();

        builder.append(getId(), o.getId());
        builder.append(getVersion(), o.getVersion());

        return builder.toComparison();
    }

    /**
     * Same as {@link #equals(Object)} but with {@code null} version meaning "any version".
     * 
     * @param extensionId the extension identifier to match
     * @return true if the passes extension id matches this extension id
     * @since 9.5RC1
     */
    public boolean matches(ExtensionId extensionId)
    {
        if (Objects.equal(getId(), extensionId.getId())) {
            if (getVersion() == null || extensionId.getVersion() == null
                || Objects.equal(getVersion(), extensionId.getVersion())) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param extensionIds the extension identifiers to match
     * @return true if one of the passes extension ids matched this extension id
     * @since 9.5RC1
     */
    public boolean matchesOne(Iterable<ExtensionId> extensionIds)
    {
        for (ExtensionId extensionId : extensionIds) {
            if (matches(extensionId)) {
                return true;
            }
        }

        return false;
    }
}
