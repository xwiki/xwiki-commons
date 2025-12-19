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

import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.internal.converter.ExtensionIdConverter;

/**
 * @version $Id$
 * @since 18.0.0RC1
 * @since 17.10.2
 */
public class ExtensionDescription
{
    private String id;

    private String version;

    /**
     * @return the extension id
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * @param id the extension id
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the extension version
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * @param version the extension version
     */
    public void setVersion(String version)
    {
        this.version = version;
    }

    /**
     * @param value the extension id/version
     */
    public void set(String value)
    {
        ExtensionId extensionId = ExtensionIdConverter.toExtensionId(value, null);

        this.id = extensionId.getId();
        this.version = extensionId.getVersion() != null ? extensionId.getVersion().getValue() : null;
    }
}
