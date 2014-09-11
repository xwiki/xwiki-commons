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
 * Base class for implementations of {@link ExtensionScmConnection}.
 *
 * @version $Id$
 * @since 6.3M1
 */
public abstract class AbstractExtensionScmConnection implements ExtensionScmConnection
{
    private String system;

    private String path;

    /**
     * @param system the system name
     * @param path the system specific path
     */
    public AbstractExtensionScmConnection(String system, String path)
    {
        this.system = system;
        this.path = path;
    }

    @Override
    public String getSystem()
    {
        return this.system;
    }

    @Override
    public String getPath()
    {
        return this.path;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ExtensionScmConnection) {
            ExtensionScmConnection connection = (ExtensionScmConnection) obj;
            return StringUtils.equals(this.system, connection.getSystem())
                && StringUtils.equals(this.path, connection.getPath());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(this.system);
        builder.append(this.path);

        return builder.toHashCode();
    }
}
