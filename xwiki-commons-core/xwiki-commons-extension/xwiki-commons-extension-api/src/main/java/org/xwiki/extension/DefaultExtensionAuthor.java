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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Default implementation of ExtensionAuthor.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class DefaultExtensionAuthor implements ExtensionAuthor
{
    /**
     * @see #getName()
     */
    private String name;

    /**
     * @see #getURLString()
     */
    private String url;

    private transient URL urlCache;

    /**
     * @param name the name of the author
     * @param url the URL of the author public profile
     */
    public DefaultExtensionAuthor(String name, URL url)
    {
        this.name = name;
        this.url = url.toExternalForm();
        this.urlCache = url;
    }

    /**
     * @param name the name of the author
     * @param url the URL of the author public profile
     * @since 8.5.6
     * @since 9.8.1
     */
    public DefaultExtensionAuthor(String name, String url)
    {
        this.name = name;
        this.url = url;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public URL getURL()
    {
        if (this.urlCache == null && this.url != null) {
            try {
                this.urlCache = new URL(this.url);
            } catch (MalformedURLException e) {
                // TODO: Should probably log something
            }
        }

        return this.urlCache;
    }

    @Override
    public String getURLString()
    {
        return this.url;
    }

    // Object

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ExtensionAuthor) {
            ExtensionAuthor otherAuthor = (ExtensionAuthor) obj;

            return StringUtils.equals(this.name, otherAuthor.getName())
                && Objects.equals(getURLString(), otherAuthor.getURLString());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(this.url);
        builder.append(this.name);

        return builder.toHashCode();
    }
}
