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
package org.xwiki.filter;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.filter.internal.FilterUtils;
import org.xwiki.stability.Unstable;

/**
 * Support any event in input and pass them to a list of filters.
 * 
 * @version $Id$
 * @since 5.2RC1
 */
@Unstable
public class CompositeFilter implements UnknownFilter
{
    private FilterDescriptorManager filterManager;

    private List<SubFilter> filters;

    private static class SubFilter
    {
        public final Object filter;

        public final FilterDescriptor descriptor;

        public SubFilter(Object filter, FilterDescriptor descriptor)
        {
            this.filter = filter;
            this.descriptor = descriptor;
        }
    }

    /**
     * @param filters the filters
     * @param filterManager the filter descriptor manager used to generate passed filter descriptors
     */
    public CompositeFilter(List< ? > filters, FilterDescriptorManager filterManager)
    {
        this.filterManager = filterManager;

        this.filters = new ArrayList<SubFilter>(filters.size());
        for (Object filter : filters) {
            this.filters.add(new SubFilter(filter, this.filterManager.getFilterDescriptor(filter.getClass())));
        }
    }

    @Override
    public void beginUnknwon(String id, FilterEventParameters parameters) throws FilterException
    {
        for (SubFilter filter : this.filters) {
            FilterUtils.sendBeginEvent(filter.filter, filter.descriptor, id, parameters);
        }
    }

    @Override
    public void endUnknwon(String id, FilterEventParameters parameters) throws FilterException
    {
        for (SubFilter filter : this.filters) {
            FilterUtils.sendEndEvent(filter.filter, filter.descriptor, id, parameters);
        }
    }

    @Override
    public void onUnknwon(String id, FilterEventParameters parameters) throws FilterException
    {
        for (SubFilter filter : this.filters) {
            FilterUtils.sendOnEvent(filter.filter, filter.descriptor, id, parameters);
        }
    }
}
