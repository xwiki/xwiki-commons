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
package org.xwiki.extension.version.internal;

import java.util.Collection;

import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.VersionRange;
import org.xwiki.extension.version.VersionRangeCollection;

/**
 * Contain some Version related toolkit methods.
 *
 * @version $Id$
 * @since 4.0M1
 */
public final class VersionUtils
{
    /**
     * Toolkit class.
     */
    private VersionUtils()
    {
        // Toolkit class
    }

    /**
     * @param str the string to look at
     * @param c the character to search in the provided string
     * @return true if the provided character is the first character of the provided string
     */
    public static boolean startsWith(String str, char c)
    {
        return str.length() > 0 && str.charAt(0) == c;
    }

    /**
     * @param str the string to look at
     * @param c the character to search in the provided string
     * @return true if the provided character is the last character of the provided string
     */
    public static boolean endsWith(String str, char c)
    {
        return str.length() > 0 && str.charAt(str.length() - 1) == c;
    }

    /**
     * Check if passed range collection is a strict version.
     * 
     * @param ranges the ranges to search into
     * @return the strict version constraint found or null if none could be found
     * @since 9.8RC1
     */
    public static Version getStrictVersion(Collection<? extends VersionRangeCollection> ranges)
    {
        for (VersionRangeCollection collection : ranges) {
            if (collection.getRanges().size() == 1) {
                VersionRange range = collection.getRanges().iterator().next();
                if (range instanceof DefaultVersionRange) {
                    DefaultVersionRange defaultRange = (DefaultVersionRange) range;
                    Version lowerBound = defaultRange.getLowerBound();
                    if (lowerBound != null && (lowerBound.equals(defaultRange.getUpperBound()))) {
                        return lowerBound;
                    }
                }
            }
        }

        return null;
    }
}
