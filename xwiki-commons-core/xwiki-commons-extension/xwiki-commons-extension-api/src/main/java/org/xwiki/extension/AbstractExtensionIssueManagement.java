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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Base class for implementations of {@link ExtensionIssueManagement}.
 *
 * @version $Id$
 * @since 6.3M1
 */
public abstract class AbstractExtensionIssueManagement implements ExtensionIssueManagement
{
    /**
     * @see #getSystem()
     */
    private String system;

    /**
     * @see #getURL()
     */
    private String url;

    /**
     * @param system the name of the issue management system (jira, bugzilla, etc.)
     * @param url the URL of that extension in the issues management system
     */
    public AbstractExtensionIssueManagement(String system, String url)
    {
        this.system = system;
        this.url = url;
    }

    @Override
    public String getSystem()
    {
        return this.system;
    }

    @Override
    public String getURL()
    {
        return this.url;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ExtensionIssueManagement) {
            ExtensionIssueManagement issueManagement = (ExtensionIssueManagement) obj;
            return StringUtils.equals(this.system, issueManagement.getSystem())
                && StringUtils.equals(this.url, issueManagement.getURL());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(this.system);
        builder.append(this.url);

        return builder.toHashCode();
    }
}
