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
package org.xwiki.diff;

import java.util.HashMap;

/**
 * Setup merge behavior.
 * 
 * @param <E> the type of compared elements
 * @version $Id$
 */
public class MergeConfiguration<E> extends HashMap<String, Object>
{
    /**
     * The name of the key used to setup the default fallback to use when finding a conflict.
     */
    public static final String KEY_FALLBACKONCONFLICT = "fallbackonconflict";

    /**
     * One of the merged versions.
     * 
     * @version $Id$
     */
    public enum Version
    {
        /**
         * The common ancestor.
         */
        PREVIOUS,

        /**
         * The new version.
         */
        NEXT,

        /**
         * The current version.
         */
        CURRENT
    }

    /**
     * @param version the version to fallback on when finding a conflict
     */
    public void setFallbackOnConflict(Version version)
    {
        put(KEY_FALLBACKONCONFLICT, version);
    }

    /**
     * @return the version to fallback on when finding a conflict
     */
    public Version getFallbackOnConflict()
    {
        // Default is Version.NEXT
        return containsKey(KEY_FALLBACKONCONFLICT) ? (Version) get(KEY_FALLBACKONCONFLICT) : Version.NEXT;
    }
}
