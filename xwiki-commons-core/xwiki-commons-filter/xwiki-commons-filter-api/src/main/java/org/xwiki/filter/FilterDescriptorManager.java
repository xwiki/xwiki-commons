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

import org.xwiki.component.annotation.Role;

/**
 * Manager {@link FilterDescriptor} related to classes.
 *
 * @version $Id$
 * @since 5.2M1
 */
@Role
public interface FilterDescriptorManager
{
    /**
     * @param interfaces the interfaces implemented by the filter
     * @return the filter descriptor
     * @since 5.2
     */
    FilterDescriptor getFilterDescriptor(Class<?>... interfaces);

    /**
     * Helper for input module taking care of calling the right event when it exist, fallback on {@link UnknownFilter}
     * or simply ignores it when the filter does not support it.
     *
     * @param <F> the class of the filter
     * @param interfaces the interfaces implemented by the filter
     * @param targetFilter the actual filter to send events to
     * @return the filter proxy
     * @since 5.2
     */
    <F> F createFilterProxy(Object targetFilter, Class<?>... interfaces);

    /**
     * Helper for input module taking care of calling the right event when it exist, fallback on {@link UnknownFilter}
     * or simply ignores it when the filter does not support it.
     *
     * @param <F> the class of the filter
     * @param interfaces the interfaces implemented by the filter
     * @param loader the class loader to define the proxy class
     * @param targetFilter the actual filter to send events to
     * @return the filter proxy
     * @since 5.2M3
     */
    <F> F createFilterProxy(Object targetFilter, ClassLoader loader, Class<?>... interfaces);

    /**
     * Helper for input module taking care of calling the right event when it exist, fallback on {@link UnknownFilter}
     * or simply ignores it when the filter does not support it.
     *
     * @param <F> the class of the filter
     * @param filters the actual filters to send events to
     * @return the filter proxy
     * @since 5.2
     */
    <F> F createCompositeFilter(Object... filters);

    /**
     * Helper for input module taking care of calling the right event when it exist, fallback on {@link UnknownFilter}
     * or simply ignores it when the filter does not support it.
     *
     * @param <F> the class of the filter
     * @param filters the actual filters to send events to
     * @param loader the class loader to define the proxy class
     * @return the filter proxy
     * @since 5.2M3
     */
    <F> F createCompositeFilter(ClassLoader loader, Object... filters);
}
