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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.extension.Extension;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.search.ExtensionQuery;
import org.xwiki.extension.repository.search.ExtensionQuery.COMPARISON;
import org.xwiki.extension.repository.search.ExtensionQuery.Filter;
import org.xwiki.extension.repository.search.ExtensionQuery.SortClause;

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
        Collection<? extends Extension> extensions)
    {
        return searchInCollection(pattern, offset, nb, extensions, false);
    }

    /**
     * @param pattern the pattern to match
     * @param offset the offset where to start returning elements
     * @param nb the number of maximum element to return
     * @param extensions the extension collection to search in
     * @param forceUnique make sure returned elements are unique
     * @return the search result
     * @since 6.4.1
     */
    public static CollectionIterableResult<Extension> searchInCollection(String pattern, int offset, int nb,
        Collection<? extends Extension> extensions, boolean forceUnique)
    {
        ExtensionQuery query = new ExtensionQuery(pattern);

        query.setOffset(offset);
        query.setLimit(nb);

        return searchInCollection(query, extensions, forceUnique);
    }

    /**
     * @param pattern the pattern to match
     * @param offset the offset where to start returning elements
     * @param nb the number of maximum element to return
     * @param extensions the extension collection to search in
     * @param forceUnique make sure returned elements are unique
     * @return the search result
     * @since 6.4.1
     */
    public static CollectionIterableResult<Extension> searchInCollection(ExtensionQuery query,
        Collection<? extends Extension> extensions, boolean forceUnique)
    {
        List<Extension> result;

        // Filter
        if (StringUtils.isEmpty(query.getQuery())) {
            result = extensions instanceof List ? (List<Extension>) extensions : new ArrayList<Extension>(extensions);
        } else {
            result = filter(query.getQuery(), query.getFilters(), extensions, forceUnique);
        }

        // Sort
        sort(result, query.getSortClauses());

        // Create result
        return RepositoryUtils.getIterableResult(query.getOffset(), query.getLimit(), result);
    }

    /**
     * @param offset the offset where to start returning elements
     * @param nb the number of maximum element to return
     * @param elements the element collection to search in
     * @return the result to limit
     * @param <T> the type of element in the {@link Collection}
     */
    public static <T> CollectionIterableResult<T> getIterableResult(int offset, int nb, Collection<T> elements)
    {
        if (nb == 0 || offset >= elements.size()) {
            return new CollectionIterableResult<T>(elements.size(), offset, Collections.<T>emptyList());
        }

        List<T> list;
        if (elements instanceof List) {
            list = (List<T>) elements;
        } else {
            list = new ArrayList<T>(elements);
        }

        return getIterableResultFromList(offset, nb, list);
    }

    /**
     * @param offset the offset where to start returning elements
     * @param nb the number of maximum element to return
     * @param elements the element collection to search in
     * @return the result to limit
     * @param <T> the type of element in the {@link List}
     */
    private static <T> CollectionIterableResult<T> getIterableResultFromList(int offset, int nb, List<T> elements)
    {
        int fromIndex = offset;
        if (fromIndex < 0) {
            fromIndex = 0;
        }

        int toIndex;
        if (nb > 0) {
            toIndex = nb + fromIndex;
            if (toIndex > elements.size()) {
                toIndex = elements.size();
            }
        } else {
            toIndex = elements.size();
        }

        return new CollectionIterableResult<T>(elements.size(), offset, elements.subList(fromIndex, toIndex));
    }

    /**
     * @param pattern the pattern to match
     * @param extensions the extension collection to search in
     * @param forceUnique make sure returned elements are unique
     * @return the filtered list of extensions
     */
    private static List<Extension> filter(String pattern, Collection<Filter> filters,
        Collection<? extends Extension> extensions, boolean forceUnique)
    {
        List<Extension> result = new ArrayList<Extension>(extensions.size());

        Pattern patternMatcher =
            Pattern.compile(SEARCH_PATTERN_SUFFIXNPREFIX + pattern.toLowerCase() + SEARCH_PATTERN_SUFFIXNPREFIX);

        for (Extension extension : extensions) {
            if (matches(patternMatcher, filters, extension)) {
                result.add(extension);
            }
        }

        // Make sure all the elements of the list are unique
        if (forceUnique && result.size() > 1) {
            result = new ArrayList<>(new LinkedHashSet<>(result));
        }

        return result;
    }

    /**
     * Matches an extension in a case insensitive way.
     *
     * @param patternMatcher the pattern to match
     * @param extension the extension to match
     * @return true if one of the element is matched
     */
    public static boolean matches(Pattern patternMatcher, Collection<Filter> filters, Extension extension)
    {
        if (matches(patternMatcher, extension.getId().getId(), extension.getDescription(), extension.getSummary(),
            extension.getName(), extension.getFeatures())) {
            for (Filter filter : filters) {
                if (!matches(filter, extension)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    public static boolean matches(Filter filter, Extension extension)
    {
        switch (filter.getField().toLowerCase()) {
            case "id":
                if (!matches(filter, extension.getId().getId())) {
                    return false;
                }
                break;
            case "version":
                if (!matches(filter, extension.getId().getVersion().toString())) {
                    return false;
                }
                break;
            case "feature":
            case "features":
                if (!matches(filter, extension.getFeatures())) {
                    return false;
                }
                break;
            case "summary":
                if (!matches(filter, extension.getSummary())) {
                    return false;
                }
                break;
            case "description":
                if (!matches(filter, extension.getDescription())) {
                    return false;
                }
                break;
            case "author":
            case "authors":
                if (!matches(filter, extension.getAuthors())) {
                    return false;
                }
                break;
            case "category":
                if (!matches(filter, extension.getCategory())) {
                    return false;
                }
                break;
            case "license":
            case "licenses":
                if (!matches(filter, extension.getLicenses())) {
                    return false;
                }
                break;
            case "name":
                if (!matches(filter, extension.getName())) {
                    return false;
                }
                break;
            case "type":
                if (!matches(filter, extension.getType())) {
                    return false;
                }
                break;
            case "website":
                if (!matches(filter, extension.getWebSite())) {
                    return false;
                }
                break;
            case "scm":
                if (!matches(filter, extension.getScm())) {
                    return false;
                }
                break;

            default:
                // Unknown field
                // FIXME: not sure if it's should be true or false in this case
                break;
        }

        return true;
    }

    public static boolean matches(Filter filter, Object element)
    {
        // TODO: add support for more than String
        if (filter.getValue() instanceof String) {
            String value = (String) filter.getValue();
            if (filter.getComparison() == COMPARISON.MATCH) {
                Pattern patternMatcher = createPatternMatcher(value);

                if (matches(patternMatcher, element)) {
                    return true;
                }
            } else if (filter.getComparison() == COMPARISON.EQUAL) {
                if (element != null && value.toLowerCase().equals(element.toString().toLowerCase())) {
                    return true;
                }
            }
        } else {
            return true;
        }

        return false;
    }

    /**
     * Matches a set of elements in a case insensitive way.
     *
     * @param patternMatcher the pattern to match
     * @param elements the elements to match
     * @return true if one of the element is matched
     */
    public static boolean matches(Pattern patternMatcher, Object... elements)
    {
        for (Object element : elements) {
            if (matches(patternMatcher, element)) {
                return true;
            }
        }

        return false;
    }

    public static boolean matches(Pattern patternMatcher, Object element)
    {
        if (element != null) {
            if (patternMatcher.matcher(element.toString().toLowerCase()).matches()) {
                return true;
            }
        }

        return false;
    }

    public static Pattern createPatternMatcher(String pattern)
    {
        return StringUtils.isEmpty(pattern) ? null : Pattern.compile(RepositoryUtils.SEARCH_PATTERN_SUFFIXNPREFIX
            + Pattern.quote(pattern.toLowerCase()) + RepositoryUtils.SEARCH_PATTERN_SUFFIXNPREFIX);
    }

    public static void sort(List<? extends Extension> extensions, Collection<SortClause> sortClauses)
    {
        for (SortClause sortClause : sortClauses) {
            sort(extensions, sortClause);
        }
    }

    public static void sort(List<? extends Extension> extensions, SortClause sortClause)
    {
        // TODO
    }
}
