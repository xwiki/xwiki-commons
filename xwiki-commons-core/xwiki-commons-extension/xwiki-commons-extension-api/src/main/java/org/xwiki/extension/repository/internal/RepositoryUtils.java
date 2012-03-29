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
package org.xwiki.extension.repository.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.xwiki.extension.Extension;
import org.xwiki.extension.repository.result.CollectionIterableResult;

/**
 * A set of Repository related tools.
 * 
 * @version $Id$
 * @since 4.0M2
 */
public final class RepositoryUtils
{
    /**
     * The suffix and prefix to add to the regex when searching for a core extension.
     */
    public static final String SEARCH_PATTERN_SUFFIXNPREFIX = ".*";

    /**
     * Utility class.
     */
    private RepositoryUtils()
    {
    }

    /**
     * @param pattern the pattern to match
     * @param offset the offset where to start returning elements
     * @param nb the number of maximum element to return
     * @param extensions the extension collection to search in
     * @return the search result
     */
    public static CollectionIterableResult<Extension> searchInCollection(String pattern, int offset, int nb,
        Collection< ? extends Extension> extensions)
    {
        Pattern patternMatcher = Pattern.compile(SEARCH_PATTERN_SUFFIXNPREFIX + pattern + SEARCH_PATTERN_SUFFIXNPREFIX);

        List<Extension> result = new ArrayList<Extension>();

        if (nb != 0 && offset < extensions.size()) {
            int i = 0;
            for (Extension extension : extensions) {
                if (i >= offset) {
                    if (matches(patternMatcher, extension)) {
                        result.add(extension);
                    }
                }
            }
        }

        return new CollectionIterableResult<Extension>(extensions.size(), offset, result);
    }

    /**
     * @param patternMatcher matches a set of elements
     * @param extension the extension to match
     * @return true if one of the element is matched
     */
    public static boolean matches(Pattern patternMatcher, Extension extension)
    {
        return matches(patternMatcher, extension.getId().getId(), extension.getDescription(), extension.getSummary(),
            extension.getName(), extension.getFeatures());
    }

    /**
     * @param patternMatcher matches a set of elements
     * @param elements the elements to match
     * @return true if one of the element is matched
     */
    public static boolean matches(Pattern patternMatcher, Object... elements)
    {
        for (Object element : elements) {
            if (patternMatcher.matcher(element.toString()).matches()) {
                return true;
            }
        }

        return false;
    }
}
