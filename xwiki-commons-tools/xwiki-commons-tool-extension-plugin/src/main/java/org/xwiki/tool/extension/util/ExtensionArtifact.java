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
package org.xwiki.tool.extension.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @version $Id$
 * @since 9.4RC1
 */
public class ExtensionArtifact
{
    private String groupId;

    private String artifactId;

    private String version;

    private String type;

    /**
     * @return the group id of the artifact
     */
    public String getGroupId()
    {
        return groupId;
    }

    /**
     * @param grouId the group id of the artifact
     */
    public void setGrouId(String grouId)
    {
        this.groupId = grouId;
    }

    /**
     * @return the artifact id of the artifact
     */
    public String getArtifactId()
    {
        return artifactId;
    }

    /**
     * @param artifactId the artifact id of the artifact
     */
    public void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
    }

    /**
     * @return the version of the artifact
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * @param version the version of the artifact
     */
    public void setVersion(String version)
    {
        this.version = version;
    }

    /**
     * @return the type of the artifact
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type the type of the artifact
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Set the artifact coordinates from a single string in the
     * {@code <groupId>:<artifactId>[:<version>[:<type>]]} format.
     *
     * @param value the artifact coordinates
     */
    public void set(String value)
    {
        String[] values = StringUtils.split(value, ':');

        setGrouId(values[0]);
        setArtifactId(values[1]);
        if (values.length >= 3) {
            setVersion(values[2]);
        }
        if (values.length >= 4) {
            setType(values[3]);
        }
    }
}
