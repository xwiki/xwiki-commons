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
package org.xwiki.extension.repository.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.xwiki.stability.Unstable;

/**
 * A query to an extension repository.
 * 
 * @version $Id$
 * @since 7.0M2
 */
@Unstable
public class ExtensionQuery
{
    public enum ORDER
    {
        DESC,
        ASC;
    }

    public enum COMPARISON
    {
        EQUAL,
        MATCH;
    }

    public static class SortClause
    {
        private final String field;

        private final ORDER order;

        public SortClause(String field, ORDER order)
        {
            this.field = field;
            this.order = order;
        }

        public String getField()
        {
            return this.field;
        }

        public ORDER getOrder()
        {
            return this.order;
        }
    }

    public static class Filter
    {
        private final String field;

        private final Object value;

        private final COMPARISON comparison;

        public Filter(String field, Object value, COMPARISON comparison)
        {
            this.field = field;
            this.value = value;
            this.comparison = comparison;
        }

        public String getField()
        {
            return this.field;
        }

        public Object getValue()
        {
            return this.value;
        }

        public COMPARISON getComparison()
        {
            return this.comparison;
        }
    }

    private String query;

    private int limit = -1;

    private int offset = 0;

    private List<SortClause> sortClauses;

    private List<Filter> filters;

    public ExtensionQuery()
    {
    }

    public ExtensionQuery(String query)
    {
        this.query = query;
    }

    /**
     * @return the query statement
     */
    public String getQuery()
    {
        return this.query;
    }

    /**
     * @param limit limit of result list to set (so {@link #execute()}.size() will be <= limit).
     * @return this query.
     */
    public ExtensionQuery setLimit(int limit)
    {
        this.limit = limit;

        return this;
    }

    /**
     * @param offset offset of query result to set (skip first "offset" rows).
     * @return this query.
     */
    public ExtensionQuery setOffset(int offset)
    {
        this.offset = offset;

        return this;
    }

    public List<Filter> getFilters()
    {
        return this.filters;
    }

    public void setFilters(Collection<Filter> filters)
    {
        this.filters = new ArrayList<>(filters);
    }

    public ExtensionQuery addFilter(String field, Object value, COMPARISON comparizon)
    {
        if (this.filters == null) {
            this.filters = new ArrayList<>();
        }

        this.filters.add(new Filter(field, value, comparizon));

        return this;
    }

    public List<SortClause> getSortClauses()
    {
        return this.sortClauses;
    }

    public void setSortClauses(Collection<SortClause> sortClauses)
    {
        this.sortClauses = new ArrayList<>(sortClauses);
    }

    public ExtensionQuery addSort(String field, ORDER order)
    {
        if (this.sortClauses == null) {
            this.sortClauses = new ArrayList<>();
        }

        this.sortClauses.add(new SortClause(field, order));

        return this;
    }

    /**
     * @return limit limit of result list.
     * @see #setLimit(int)
     */
    public int getLimit()
    {
        return this.limit;
    }

    /**
     * @return offset offset of query result.
     * @see #setOffset(int)
     */
    public int getOffset()
    {
        return this.offset;
    }
}
