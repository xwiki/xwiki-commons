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
package org.xwiki.filter.type;

import java.util.Objects;

/**
 * Represents various systems.
 *
 * @version $Id$
 * @since 6.2M1
 */
public class SystemType
{
    /**
     * MediaWiki wiki type.
     */
    public static final SystemType MEDIAWIKI = new SystemType("mediawiki");

    /**
     * DokuWiki wiki type.
     * 
     * @since 9.5RC1
     */
    public static final SystemType DOKUWIKI = new SystemType("dokuwiki");

    /**
     * Confluence wiki type.
     */
    public static final SystemType CONFLUENCE = new SystemType("confluence");

    /**
     * XWiki wiki type.
     */
    public static final SystemType XWIKI = new SystemType("xwiki");

    /**
     * Generic filter.
     */
    public static final SystemType FILTER = new SystemType("filter");

    /**
     * Id of a Wiki.
     */
    private String id;

    /**
     * @param id of a wiki
     */
    public SystemType(String id)
    {
        this.id = id.toLowerCase();
    }

    /**
     * Create a new {@link SystemType} from a {@link String}.
     *
     * @param str the {@link String} to parse
     * @return a {@link SystemType}
     */
    public static SystemType unserialize(String str)
    {
        return new SystemType(str);
    }

    /**
     * @return id of the wiki
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * @return a {@link String} representation of the {@link FilterStreamType}
     */
    public String serialize()
    {
        return getId();
    }

    @Override
    public String toString()
    {
        return serialize();
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object object)
    {
        boolean result;

        if (this == object) {
            result = true;
        } else {
            if (object instanceof SystemType) {
                result = Objects.equals(getId(), ((SystemType) object).getId());
            } else {
                result = false;
            }
        }

        return result;
    }
}
