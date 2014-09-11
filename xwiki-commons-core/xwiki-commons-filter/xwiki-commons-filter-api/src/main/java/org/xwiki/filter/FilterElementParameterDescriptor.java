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

import java.lang.reflect.Type;

import org.xwiki.stability.Unstable;

/**
 * A filter element parameter.
 *
 * @param <T> the type of the parameter
 * @version $Id$
 * @since 5.2M1
 */
@Unstable
public class FilterElementParameterDescriptor<T>
{
    /**
     * @see #getIndex()
     */
    private int index;

    /**
     * @see #getName()
     */
    private String name;

    /**
     * @see #getType()
     */
    private Type type;

    /**
     * @see #getDefaultValue()
     */
    private T defaultValue;

    /**
     * @param index the index of the parameter.
     * @param name the name of the parameter. {@code null} if no {@link org.xwiki.filter.annotation.Name} annotation has
     *            been used.
     * @param type the type of the parameter.
     * @param defaultValue the default value.
     */
    public FilterElementParameterDescriptor(int index, String name, Type type, T defaultValue)
    {
        this.index = index;
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    /**
     * @return the index of the parameter.
     */
    public int getIndex()
    {
        return this.index;
    }

    /**
     * @return the name of the parameter. {@code null} if no {@link org.xwiki.filter.annotation.Name} annotation has
     *         been used.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the type of the parameter.
     */
    public Type getType()
    {
        return this.type;
    }

    /**
     * @return the default value.
     */
    public T getDefaultValue()
    {
        return this.defaultValue;
    }
}
