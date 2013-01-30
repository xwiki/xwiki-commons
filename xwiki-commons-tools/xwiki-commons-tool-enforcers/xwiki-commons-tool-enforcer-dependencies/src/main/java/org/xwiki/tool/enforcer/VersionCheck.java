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
package org.xwiki.tool.enforcer;

/**
 * A single check for verifying a dependency version. The Rule will check that any dependency starting with the
 * specified group Id prefix will have a version that matches the specified version regex.
 *
 * @version $Id$
 * @since 4.5RC1
 */
public class VersionCheck
{
    /**
     * @see #getGroupIdPrefix()
     */
    private String groupIdPrefix;

    /**
     * @see #getAllowedVersionRegex()
     */
    public String allowedVersionRegex;

    /**
     * @param groupIdPrefix see {@link #getGroupIdPrefix()}
     */
    public void setGroupIdPrefix(String groupIdPrefix)
    {
        this.groupIdPrefix = groupIdPrefix;
    }

    /**
     * Note: This method is called automatically by the Maven Enforcer plugin framework.
     *
     * @return the group id prefix to check against (eg "org.xwiki.commons")
     */
    public String getGroupIdPrefix()
    {
        return this.groupIdPrefix;
    }

    /**
     * @param allowedVersionRegex see {@link #getAllowedVersionRegex()}
     */
    public void setAllowedVersionRegex(String allowedVersionRegex)
    {
        this.allowedVersionRegex = allowedVersionRegex;
    }

    /**
     * Note: This method is called automatically by the Maven Enforcer plugin framework.
     *
     * @return the regex to match the version dependency against for matching group ids
     */
    public String getAllowedVersionRegex()
    {
        return this.allowedVersionRegex;
    }
}
