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

import org.xwiki.filter.FilterDescriptor;
import org.xwiki.filter.FilterElementDescriptor;
import org.xwiki.filter.FilterElementParameterDescriptor;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.UnknownFilter;

/**
 * Helper for input module taking care of calling the right event when it exist, fallback on {@link UnknownFilter} or
 * simply ignores it when the filter does not support it.
 *
 * @version $Id$
 * @since 5.2M1
 */
public final class FilterProxy implements InvocationHandler
{
    /**
     * The descriptor of the reference filter.
     */
    private FilterDescriptor descriptor;

    private Object targetFilter;

    /**
     * @param filter the actual filter to send events to
     * @param descriptor the reference filter descriptor
     */
    public FilterProxy(Object filter, FilterDescriptor descriptor)
    {
        this.targetFilter = filter;
        this.descriptor = descriptor;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        try {
            invoke(this.targetFilter, this.descriptor, method, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }

        return null;
    }

    /**
     * @param filter the filter to send event to
     * @param descriptor the descriptor of the filter
     * @param method the event method called
     * @param args the arguments of the called method
     * @exception IllegalAccessException if this <code>Method</code> object enforces Java language access control and
     *                the underlying method is inaccessible.
     * @exception IllegalArgumentException if the method is an instance method and the specified object argument is not
     *                an instance of the class or interface declaring the underlying method (or of a subclass or
     *                implementor thereof); if the number of actual and formal parameters differ; if an unwrapping
     *                conversion for primitive arguments fails; or if, after possible unwrapping, a parameter value
     *                cannot be converted to the corresponding formal parameter type by a method invocation conversion.
     * @exception InvocationTargetException if the underlying method throws an exception.
     * @throws FilterException if the execution of the event failed
     */
    public static void invoke(Object filter, FilterDescriptor descriptor, Method method, Object[] args)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, FilterException
    {
        if (method.getDeclaringClass().isInstance(filter)) {
            method.invoke(filter, args);
        } else if (filter instanceof UnknownFilter) {
            invokeUnkown(filter, descriptor, method, args);
        }
    }

    private static void invokeUnkown(Object filter, FilterDescriptor descriptor, Method method, Object[] args)
        throws FilterException
    {
        String methodName = method.getName();

        String id = DefaultFilterDescriptorManager.getElementName(methodName);

        if (id != null) {
            FilterElementDescriptor element = descriptor.getElement(id);

            if (element != null) {
                FilterEventParameters metadata = new FilterEventParameters();

                for (FilterElementParameterDescriptor<?> parameter : element.getParameters()) {
                    metadata.put(
                        parameter.getName() != null ? parameter.getName() : String.valueOf(parameter.getIndex()),
                        args[parameter.getIndex()]);
                }

                UnknownFilter unknownFilter = (UnknownFilter) filter;
                if (methodName.startsWith(DefaultFilterDescriptorManager.PREFIX_BEGIN)) {
                    unknownFilter.beginUnknwon(id, metadata);
                } else if (methodName.startsWith(DefaultFilterDescriptorManager.PREFIX_END)) {
                    unknownFilter.endUnknwon(id, metadata);
                } else if (methodName.startsWith(DefaultFilterDescriptorManager.PREFIX_ON)) {
                    unknownFilter.onUnknwon(id, metadata);
                }
            }
        }
    }
}
