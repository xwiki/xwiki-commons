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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.extension.Extension;
import org.xwiki.extension.internal.converter.ExtensionIdConverter;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.result.AggregatedIterableResult;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.AdvancedSearchable;
import org.xwiki.extension.repository.search.ExtensionQuery;
import org.xwiki.extension.repository.search.ExtensionQuery.COMPARISON;
import org.xwiki.extension.repository.search.ExtensionQuery.Filter;
import org.xwiki.extension.repository.search.ExtensionQuery.SortClause;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.repository.search.Searchable;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryUtils.class);

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
     * @param <E> the type of element in the {@link Collection}
     */
    public static <E extends Extension> CollectionIterableResult<E> searchInCollection(String pattern, int offset,
        int nb, Collection<E> extensions)
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
     * @param <E> the type of element in the {@link Collection}
     */
    public static <E extends Extension> CollectionIterableResult<E> searchInCollection(String pattern, int offset,
        int nb, Collection<E> extensions, boolean forceUnique)
    {
        ExtensionQuery query = new ExtensionQuery(pattern);

        query.setOffset(offset);
        query.setLimit(nb);

        return searchInCollection(query, extensions, forceUnique);
    }

    /**
     * @param query the query
     * @param extensions the extension collection to search in
     * @param forceUnique make sure returned elements are unique
     * @return the search result
     * @since 7.0M2
     * @param <E> the type of element in the {@link Collection}
     */
    public static <E extends Extension> CollectionIterableResult<E> searchInCollection(ExtensionQuery query,
        Collection<E> extensions, boolean forceUnique)
    {
        List<E> result;

        // Filter
        if (StringUtils.isEmpty(query.getQuery())) {
            result = extensions instanceof List ? (List<E>) extensions : new ArrayList<>(extensions);
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
     * @param <E> the type of element in the {@link Collection}
     */
    public static <E> CollectionIterableResult<E> getIterableResult(int offset, int nb, Collection<E> elements)
    {
        if (nb == 0 || offset >= elements.size()) {
            return new CollectionIterableResult<>(elements.size(), offset, Collections.<E>emptyList());
        }

        List<E> list;
        if (elements instanceof List) {
            list = (List<E>) elements;
        } else {
            list = new ArrayList<>(elements);
        }

        return getIterableResultFromList(offset, nb, list);
    }

    /**
     * @param offset the offset where to start returning elements
     * @param nb the number of maximum element to return
     * @param elements the element collection to search in
     * @return the result to limit
     * @param <E> the type of element in the {@link List}
     */
    private static <E> CollectionIterableResult<E> getIterableResultFromList(int offset, int nb, List<E> elements)
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

        return new CollectionIterableResult<>(elements.size(), offset, elements.subList(fromIndex, toIndex));
    }

    /**
     * @param pattern the pattern to match
     * @param filters the filters
     * @param extensions the extension collection to search in
     * @param forceUnique make sure returned elements are unique
     * @return the filtered list of extensions
     * @since 7.0M2
     * @param <E> the type of element in the {@link Collection}
     */
    private static <E extends Extension> List<E> filter(String pattern, Collection<Filter> filters,
        Collection<E> extensions, boolean forceUnique)
    {
        List<E> result = new ArrayList<>(extensions.size());

        Pattern patternMatcher =
            Pattern.compile(SEARCH_PATTERN_SUFFIXNPREFIX + pattern.toLowerCase() + SEARCH_PATTERN_SUFFIXNPREFIX);

        for (E extension : extensions) {
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
     * @param filters the filters
     * @param extension the extension to match
     * @return false if one of the filter is not matching the extension
     * @since 7.0M2
     */
    public static boolean matches(Pattern patternMatcher, Collection<Filter> filters, Extension extension)
    {
        if (matches(patternMatcher, extension.getId().getId(), extension.getDescription(), extension.getSummary(),
            extension.getName(), ExtensionIdConverter.toStringList(extension.getExtensionFeatures()))) {
            for (Filter filter : filters) {
                if (!matches(filter, extension)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Make sure the passed extension matches all filters.
     *
     * @param filters the filters
     * @param extension the extension to match
     * @return false if one of the filter is not matching the extension
     * @since 8.3RC1
     */
    public static boolean matches(Collection<Filter> filters, Extension extension)
    {
        if (filters != null) {
            for (Filter filter : filters) {
                if (!matches(filter, extension)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * @param filter the filter
     * @param extension the extension to match
     * @return true if the extension is matched by the filer
     * @since 7.0M2
     */
    public static boolean matches(Filter filter, Extension extension)
    {
        return matches(filter, extension.<Object>get(filter.getField()));
    }

    /**
     * @param filter the filter
     * @param element the element to match
     * @return true if the element is matched by the filer
     * @since 7.0M2
     */
    public static boolean matches(Filter filter, Object element)
    {
        if (element == null) {
            return filter.getValue() == null;
        } else if (filter.getValue() == null) {
            return false;
        }

        // TODO: add support for more than String
        String filterValue = String.valueOf(filter.getValue());
        String elementValue = String.valueOf(element);

        if (filter.getComparison() == COMPARISON.MATCH) {
            Pattern patternMatcher = createPatternMatcher(filterValue);

            if (matches(patternMatcher, elementValue)) {
                return true;
            }
        } else if (filter.getComparison() == COMPARISON.EQUAL) {
            if (filterValue.equals(elementValue)) {
                return true;
            }
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
        if (patternMatcher == null) {
            return true;
        }

        for (Object element : elements) {
            if (matches(patternMatcher, element)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param patternMatcher the pattern to match
     * @param element the element to match with the pattern
     * @return true of the element is matched by the pattern
     */
    public static boolean matches(Pattern patternMatcher, Object element)
    {
        if (element != null) {
            if (patternMatcher.matcher(element.toString().toLowerCase()).matches()) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param pattern the pattern to match
     * @return a {@link Pattern} used to search the passed pattern inside a {@link String}
     */
    public static Pattern createPatternMatcher(String pattern)
    {
        return StringUtils.isEmpty(pattern) ? null : Pattern.compile(RepositoryUtils.SEARCH_PATTERN_SUFFIXNPREFIX
            + Pattern.quote(pattern.toLowerCase()) + RepositoryUtils.SEARCH_PATTERN_SUFFIXNPREFIX);
    }

    /**
     * Sort the passed extensions list based on the passed sort clauses.
     * 
     * @param extensions the list of extensions to sort
     * @param sortClauses the sort clauses
     * @since 7.0M2
     */
    public static void sort(List<? extends Extension> extensions, Collection<SortClause> sortClauses)
    {
        Collections.sort(extensions, new SortClauseComparator(sortClauses));
    }

    /**
     * Merge provided search results.
     * 
     * @param previousSearchResult all the previous search results
     * @param result the new search result to append
     * @return the new aggregated search result
     * @since 8.1M1
     * @param <E> the type of element in the {@link Collection}
     */
    public static <E extends Extension> IterableResult<E> appendSearchResults(IterableResult<E> previousSearchResult,
        IterableResult<E> result)
    {
        AggregatedIterableResult<E> newResult;

        if (previousSearchResult instanceof AggregatedIterableResult) {
            newResult = ((AggregatedIterableResult<E>) previousSearchResult);
        } else if (previousSearchResult != null) {
            newResult = new AggregatedIterableResult<>(previousSearchResult.getOffset());
            newResult.addSearchResult(previousSearchResult);
        } else {
            return result;
        }

        newResult.addSearchResult(result);

        return newResult;
    }

    /**
     * Search passed repositories based of the provided query.
     * 
     * @param query the query
     * @param repositories the repositories
     * @return the found extensions descriptors, empty list if nothing could be found
     * @throws SearchException error when trying to search provided query
     * @since 10.0RC1
     */
    public static IterableResult<Extension> search(ExtensionQuery query, Iterable<ExtensionRepository> repositories)
        throws SearchException
    {
        IterableResult<Extension> searchResult = null;

        int currentOffset = query.getOffset() > 0 ? query.getOffset() : 0;
        int currentNb = query.getLimit();

        // A local index would avoid things like this...
        for (ExtensionRepository repository : repositories) {
            try {
                searchResult = search(repository, query, currentOffset, currentNb, searchResult);

                if (searchResult != null) {
                    if (currentOffset > 0) {
                        currentOffset = query.getOffset() - searchResult.getTotalHits();
                        if (currentOffset < 0) {
                            currentOffset = 0;
                        }
                    }

                    if (currentNb > 0) {
                        currentNb = query.getLimit() - searchResult.getSize();
                        if (currentNb < 0) {
                            currentNb = 0;
                        }
                    }
                }
            } catch (SearchException e) {
                LOGGER.error(
                    "Failed to search on repository [{}] with query [{}]. " + "Ignore and go to next repository.",
                    repository.getDescriptor().toString(), query, e);
            }
        }

        return searchResult != null ? searchResult
            : new CollectionIterableResult<>(0, query.getOffset(), Collections.<Extension>emptyList());
    }

    private static IterableResult<Extension> search(ExtensionRepository repository, ExtensionQuery query,
        int currentOffset, int currentNb, IterableResult<Extension> previousSearchResult) throws SearchException
    {
        ExtensionQuery customQuery = query;
        if (currentOffset != customQuery.getOffset() && currentNb != customQuery.getLimit()) {
            customQuery = new ExtensionQuery(query);
            customQuery.setOffset(currentOffset);
            customQuery.setLimit(currentNb);
        }

        return search(repository, customQuery, previousSearchResult);
    }

    /**
     * Search one repository.
     *
     * @param repository the repository to search
     * @param query the search query
     * @param previousSearchResult the current search result merged from all previous repositories
     * @return the updated maximum number of search results to return
     * @throws SearchException error while searching on provided repository
     */
    public static IterableResult<Extension> search(ExtensionRepository repository, ExtensionQuery query,
        IterableResult<Extension> previousSearchResult) throws SearchException
    {
        IterableResult<Extension> result;

        if (repository instanceof Searchable) {
            if (repository instanceof AdvancedSearchable) {
                AdvancedSearchable searchableRepository = (AdvancedSearchable) repository;

                result = searchableRepository.search(query);
            } else {
                Searchable searchableRepository = (Searchable) repository;

                result = searchableRepository.search(query.getQuery(), query.getOffset(), query.getLimit());
            }

            if (previousSearchResult != null) {
                result = RepositoryUtils.appendSearchResults(previousSearchResult, result);
            }
        } else {
            result = previousSearchResult;
        }

        return result;
    }
}
