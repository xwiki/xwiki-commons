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

import java.util.Collection;
import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;
import org.xwiki.extension.Extension;
import org.xwiki.extension.repository.search.ExtensionQuery.ORDER;
import org.xwiki.extension.repository.search.ExtensionQuery.SortClause;

/**
 * A custom {@link Comparator} for {@link Extension} based on {@link SortClause}.
 * 
 * @version $Id$
 * @since 7.0M2
 */
public class SortClauseComparator implements Comparator<Extension>
{
    private final Collection<SortClause> sortClauses;

    /**
     * @param sortClauses the sort clauses
     */
    public SortClauseComparator(Collection<SortClause> sortClauses)
    {
        this.sortClauses = sortClauses;
    }

    @Override
    public int compare(Extension o1, Extension o2)
    {
        for (SortClause sortClause : this.sortClauses) {
            int result = compare(o1, o2, sortClause);

            if (result != 0) {
                return result;
            }
        }

        return 0;
    }

    private int compare(Extension o1, Extension o2, SortClause sortClause)
    {
        Object value1 = o1.get(sortClause.getField());
        Object value2 = o2.get(sortClause.getField());

        if (value1 instanceof Comparable && value2 instanceof Comparable) {
            return ObjectUtils.compare((Comparable) value1, (Comparable) value2)
                + (sortClause.getOrder() == ORDER.ASC ? 1 : -1);
        }

        return 0;
    }
}
