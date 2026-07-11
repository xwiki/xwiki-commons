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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * Base class or implementations of {@link ExtensionScm}.
 *
 * @version $Id$
 * @since 6.3M1
 */
public abstract class AbstractExtensionScm implements ExtensionScm
{
    private final ExtensionScmConnection connection;

    private final ExtensionScmConnection developerConnection;

    private final String url;

    private final String tag;

    /**
     * @param url the browsable URL
     * @param connection the read connection
     * @param developerConnection the write connection
     */
    public AbstractExtensionScm(String url, ExtensionScmConnection connection,
        ExtensionScmConnection developerConnection)
    {
        this(url, connection, developerConnection, null);
    }

    /**
     * @param url the browsable URL
     * @param connection the read connection
     * @param developerConnection the write connection
     * @param tag the tag corresponding to the extension's version
     */
    // Keep this constructor public: reducing its visibility to protected would break backward compatibility
    // (a source/binary incompatible change flagged by Revapi).
    @SuppressWarnings("java:S5993")
    public AbstractExtensionScm(String url, ExtensionScmConnection connection,
        ExtensionScmConnection developerConnection, String tag)
    {
        this.url = url;
        this.connection = connection;
        this.developerConnection = developerConnection;
        this.tag = tag;
    }

    @Override
    public ExtensionScmConnection getConnection()
    {
        return this.connection;
    }

    @Override
    public ExtensionScmConnection getDeveloperConnection()
    {
        return this.developerConnection;
    }

    @Override
    public String getUrl()
    {
        return this.url;
    }

    @Override
    public String getTag()
    {
        return this.tag;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ExtensionScm scm) {
            EqualsBuilder builder = new EqualsBuilder();

            builder.append(this.url, scm.getUrl());
            builder.append(this.connection, scm.getConnection());
            builder.append(this.developerConnection, scm.getDeveloperConnection());
            builder.append(this.tag, scm.getTag());

            return builder.isEquals();
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(this.connection);
        builder.append(this.developerConnection);
        builder.append(this.url);
        builder.append(this.tag);

        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new XWikiToStringBuilder(this);

        builder.append(this.connection);
        builder.append(this.developerConnection);
        builder.append(this.url);
        builder.append(this.tag);

        return builder.toString();
    }
}
