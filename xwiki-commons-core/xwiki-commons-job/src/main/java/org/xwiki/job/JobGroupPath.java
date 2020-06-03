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
package org.xwiki.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * A job group path/identifier.
 *
 * @version $Id$
 * @since 6.1M2
 */
public class JobGroupPath implements Serializable
{
    private final JobGroupPath parent;

    private final List<String> path;

    /**
     * @param path the path
     */
    public JobGroupPath(Collection<String> path)
    {
        this.path = Collections.unmodifiableList(new ArrayList<>(path));

        // Build parent
        if (this.path.size() > 1) {
            this.parent = new JobGroupPath(this.path.subList(0, this.path.size() - 1));
        } else {
            this.parent = null;
        }
    }

    /**
     * @param element the last element of the path
     * @param parent the parent
     */
    public JobGroupPath(String element, JobGroupPath parent)
    {
        this.parent = parent;

        // Build path
        List<String> list;
        if (parent == null) {
            list = new ArrayList<>(1);
        } else {
            list = new ArrayList<>(parent.getPath().size() + 1);
            list.addAll(parent.getPath());
        }

        list.add(element);

        this.path = Collections.unmodifiableList(list);
    }

    /**
     * @return the path as list
     */
    public List<String> getPath()
    {
        return this.path;
    }

    /**
     * @return the parent group
     */
    public JobGroupPath getParent()
    {
        return this.parent;
    }

    @Override
    public int hashCode()
    {
        return this.path.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof JobGroupPath) {
            return this.path.equals(((JobGroupPath) obj).getPath());
        }

        return false;
    }

    @Override
    public String toString()
    {
        return StringUtils.join(this.path, '/');
    }
}
