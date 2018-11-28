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
package org.xwiki.properties;

import java.util.Collections;
import java.util.List;

/**
 * Contains a {@code List<String>} to view the group as a single object and the associated feature.
 *
 * @version $Id$
 * @since 10.11RC1
 */
public class PropertyGroupDescriptor
{
    private List<String> group;

    private String feature;

    /**
     * Default constructor.
     *
     * @param group hierarchy of groups or null
     */
    public PropertyGroupDescriptor(List<String> group)
    {
        if (group == null) {
            this.group = null;
        } else {
            this.group = Collections.unmodifiableList(group);
        }
    }

    /**
     * @return the hierarchy of groups or null
     */
    public List<String> getGroup()
    {
        return group;
    }

    /**
     * @param feature the feature to associate to this group
     */
    public void setFeature(String feature)
    {
        this.feature = feature;
    }

    /**
     * @return the feature associated to this group or null
     */
    public String getFeature()
    {
        return feature;
    }
}
