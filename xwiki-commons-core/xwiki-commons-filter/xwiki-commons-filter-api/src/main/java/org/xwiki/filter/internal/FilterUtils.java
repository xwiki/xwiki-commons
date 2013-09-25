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

import java.lang.reflect.Method;

import org.xwiki.filter.FilterDescriptor;
import org.xwiki.filter.FilterElementDescriptor;
import org.xwiki.filter.FilterElementParameterDescriptor;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.UnknownFilter;

/**
 * Provide various filter related tools.
 * 
 * @version $Id$
 */
public final class FilterUtils
{
    private FilterUtils()
    {
        // Utility class
    }

    /**
     * Call passed begin event if possible.
     * 
     * @param event the event to send
     * @param elementDescriptor the descriptor of the event to send
     * @param filter the filter
     * @param parameters the parameters of the event
     * @return true if the event has been sent, false otherwise
     * @throws FilterException when the passed filter exposes the event but failed anyway
     */
    public static boolean sendEvent(Method event, FilterElementDescriptor elementDescriptor, Object filter,
        FilterEventParameters parameters) throws FilterException
    {
        FilterElementParameterDescriptor< ? >[] parameterDescriptors = elementDescriptor.getParameters();

        Object[] arguments = new Object[parameterDescriptors.length];

        for (FilterElementParameterDescriptor< ? > parameterDescriptor : parameterDescriptors) {
            Object value;
            if (parameterDescriptor.getName() != null && parameters.containsKey(parameterDescriptor.getName())) {
                value = parameters.get(parameterDescriptor.getName());
            } else if (parameters.containsKey(String.valueOf(parameterDescriptor.getIndex()))) {
                value = parameters.get(String.valueOf(parameterDescriptor.getIndex()));
            } else {
                value = parameterDescriptor.getDefaultValue();
            }

            arguments[parameterDescriptor.getIndex()] = value;
        }

        try {
            event.invoke(filter, arguments);
        } catch (Exception e) {
            throw new FilterException(String.format("Failed to send event [%s] with parameters [%s] to filter [%s]",
                event, parameters, filter));
        }

        return true;
    }

    /**
     * Call passed begin event if possible.
     * 
     * @param filter the filter
     * @param descriptor the descriptor of the filter
     * @param id the id of the event
     * @param parameters the parameters of the event
     * @return true if the event has been sent, false otherwise
     * @throws FilterException when the passed filter exposes the event but failed anyway
     */
    public static boolean sendBeginEvent(Object filter, FilterDescriptor descriptor, String id,
        FilterEventParameters parameters) throws FilterException
    {
        FilterElementDescriptor elementDescriptor = descriptor.getElement(id);

        if (elementDescriptor != null && elementDescriptor.getBeginMethod() != null) {
            sendEvent(elementDescriptor.getBeginMethod(), elementDescriptor, filter, parameters);
        } else if (filter instanceof UnknownFilter) {
            ((UnknownFilter) filter).beginUnknwon(id, parameters);
        } else {
            return false;
        }

        return true;
    }

    /**
     * Call passed end event if possible.
     * 
     * @param filter the filter
     * @param descriptor the descriptor of the filter
     * @param id the id of the event
     * @param parameters the parameters of the event
     * @return true if the event has been sent, false otherwise
     * @throws FilterException when the passed filter exposes the event but failed anyway
     */
    public static boolean sendEndEvent(Object filter, FilterDescriptor descriptor, String id,
        FilterEventParameters parameters) throws FilterException
    {
        FilterElementDescriptor elementDescriptor = descriptor.getElement(id);

        if (elementDescriptor != null && elementDescriptor.getEndMethod() != null) {
            sendEvent(elementDescriptor.getEndMethod(), elementDescriptor, filter, parameters);
        } else if (filter instanceof UnknownFilter) {
            ((UnknownFilter) filter).endUnknwon(id, parameters);
        } else {
            return false;
        }

        return true;
    }

    /**
     * Call passed on event if possible.
     * 
     * @param filter the filter
     * @param descriptor the descriptor of the filter
     * @param id the id of the event
     * @param parameters the parameters of the event
     * @return true if the event has been sent, false otherwise
     * @throws FilterException when the passed filter exposes the event but failed anyway
     */
    public static boolean sendOnEvent(Object filter, FilterDescriptor descriptor, String id,
        FilterEventParameters parameters) throws FilterException
    {
        FilterElementDescriptor elementDescriptor = descriptor.getElement(id);

        if (elementDescriptor != null && elementDescriptor.getOnMethod() != null) {
            sendEvent(elementDescriptor.getOnMethod(), elementDescriptor, filter, parameters);
        } else if (filter instanceof UnknownFilter) {
            ((UnknownFilter) filter).onUnknwon(id, parameters);
        } else {
            return false;
        }

        return true;
    }
}
