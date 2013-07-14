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

/**
 * A filter element parameter.
 * 
 * @version $Id$
 * @since 5.2M1
 */
public class FilterElementParameter
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
     * @param index the index of the parameter.
     * @param name the name of the parameter. {@code null} if no {@link org.xwiki.filter.annotation.Name} annotation has
     *            been used.
     * @param type the type of the parameter.
     */
    public FilterElementParameter(int index, String name, Type type)
    {
        this.index = index;
        this.name = name;
        this.type = type;
    }

    /**
     * @return the index of the parameter.
     */
    public int getIndex()
    {
        return index;
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
}
