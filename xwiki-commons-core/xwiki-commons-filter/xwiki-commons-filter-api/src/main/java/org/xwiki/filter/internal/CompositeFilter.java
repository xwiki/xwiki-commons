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
package org.xwiki.filter.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.xwiki.filter.FilterDescriptor;
import org.xwiki.filter.FilterDescriptorManager;

/**
 * Support any event in input and pass them to a list of filters.
 *
 * @version $Id$
 * @since 5.2
 */
public class CompositeFilter implements InvocationHandler
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
    public CompositeFilter(FilterDescriptorManager filterManager, Object... filters)
    {
        this.filterManager = filterManager;

        this.filters = new ArrayList<>(filters.length);
        for (Object filter : filters) {
            this.filters.add(new SubFilter(filter, this.filterManager.getFilterDescriptor(filter.getClass())));
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        for (SubFilter filter : this.filters) {
            try {
                FilterProxy.invoke(filter.filter, filter.descriptor, method, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        return null;
    }
}
