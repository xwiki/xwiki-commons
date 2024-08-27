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

import java.net.URL;
import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.xwiki.stability.Unstable;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * Default implementation of {@link ExtensionSupporter}.
 *
 * @version $Id$
 * @since 16.7.0RC1
 */
@Unstable
public class DefaultExtensionSupportPlan implements ExtensionSupportPlan
{
    /**
     * @see #getSupporter()
     */
    private final ExtensionSupporter supporter;

    /**
     * @see #getName()
     */
    private final String name;

    /**
     * @see #getURL()
     */
    private final URL url;

    /**
     * @see #isPaying()
     */
    private final boolean paying;

    /**
     * @param supporter the supporter
     * @param name the name of the author
     * @param url the URL of the author public profile
     * @param paying indicate if the plan is paying or free of charge
     */
    public DefaultExtensionSupportPlan(ExtensionSupporter supporter, String name, URL url, boolean paying)
    {
        this.supporter = supporter;
        this.name = name;
        this.url = url;
        this.paying = paying;
    }

    /**
     * @return the supporter
     */
    @Override
    public ExtensionSupporter getSupporter()
    {
        return this.supporter;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public URL getURL()
    {
        return this.url;
    }

    @Override
    public boolean isPaying()
    {
        return this.paying;
    }

    // Object

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ExtensionSupportPlan) {
            ExtensionSupportPlan otherSupportPlan = (ExtensionSupportPlan) obj;

            EqualsBuilder builder = new EqualsBuilder();

            builder.append(getSupporter(), otherSupportPlan.getSupporter());
            builder.append(getName(), otherSupportPlan.getName());
            builder.append(Objects.toString(getURL()), Objects.toString(otherSupportPlan.getURL()));
            builder.append(isPaying(), otherSupportPlan.isPaying());

            return builder.isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(getSupporter());
        builder.append(Objects.toString(getURL()));
        builder.append(getName());
        builder.append(isPaying());

        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new XWikiToStringBuilder(this);

        builder.append("supporter", getSupporter());
        builder.append("name", getName());
        builder.append("paying", isPaying());
        builder.append("url", getURL());

        return builder.toString();
    }
}
