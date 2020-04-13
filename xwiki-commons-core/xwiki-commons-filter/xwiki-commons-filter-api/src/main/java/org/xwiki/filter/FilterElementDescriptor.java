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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * An element of the filter.
 * <p>
 * An element is defined by either an <code>on</code> event of a combination of <code>begin</code> and <code>end</code>
 * events.
 *
 * @version $Id$
 * @since 5.2M1
 */
public class FilterElementDescriptor
{
    /**
     * Empty parameters.
     */
    private static final FilterElementParameterDescriptor<?>[] EMPTY_PARAMETERS =
        new FilterElementParameterDescriptor<?>[0];

    /**
     * @see #getName()
     */
    private String name;

    /**
     * @see #getParameters()
     */
    private FilterElementParameterDescriptor<?>[] parameters;

    /**
     * Used to find parameter index by name.
     */
    private Map<String, Integer> parametersIndex = new HashMap<>();

    /**
     * @see #getBeginMethod()
     */
    private Method beginMethod;

    /**
     * @see #getEndMethod()
     */
    private Method endMethod;

    /**
     * @see #getOnMethod()
     */
    private Method onMethod;

    /**
     * @param name the name of the element
     */
    public FilterElementDescriptor(String name)
    {
        this(name, EMPTY_PARAMETERS);
    }

    /**
     * @param name the name of the element
     * @param parameters the parameters
     */
    public FilterElementDescriptor(String name, FilterElementParameterDescriptor<?>[] parameters)
    {
        this.name = name;
        this.parameters = parameters;
        for (FilterElementParameterDescriptor<?> parameter : parameters) {
            if (parameter.getName() != null) {
                this.parametersIndex.put(parameter.getName(), parameter.getIndex());
            }
        }
    }

    /**
     * @return the name of the element
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the parameters of the element
     */
    public FilterElementParameterDescriptor<?>[] getParameters()
    {
        return this.parameters;
    }

    /**
     * @param <T> the type of the parameter value
     * @param name the name of the parameter
     * @return the parameter associated to the passed name
     */
    public <T> FilterElementParameterDescriptor<T> getParameter(String name)
    {
        Integer index = this.parametersIndex.get(name);

        return index != null ? (FilterElementParameterDescriptor<T>) this.parameters[index] : null;
    }

    /**
     * @return the begin method, null if it's a <code>on</code> event based element
     */
    public Method getBeginMethod()
    {
        return this.beginMethod;
    }

    /**
     * @param beginMethod the begin method, null if it's a <code>on</code> event based element
     */
    public void setBeginMethod(Method beginMethod)
    {
        this.beginMethod = beginMethod;
    }

    /**
     * @return the end method, null if it's a <code>on</code> event based element
     */
    public Method getEndMethod()
    {
        return this.endMethod;
    }

    /**
     * @param endMethod the end method, null if it's a <code>on</code> event based element
     */
    public void setEndMethod(Method endMethod)
    {
        this.endMethod = endMethod;
    }

    /**
     * @return the on method, null if it's a <code>begin/end</code> event based element
     */
    public Method getOnMethod()
    {
        return this.onMethod;
    }

    /**
     * @param onMethod the on method, null if it's a <code>begin/end</code> event based element
     */
    public void setOnMethod(Method onMethod)
    {
        this.onMethod = onMethod;
    }
}
