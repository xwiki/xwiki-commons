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
package org.xwiki.component.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 * Basic implementation of {@link ParameterizedType}.
 *
 * @version $Id$
 */
public class DefaultParameterizedType implements ParameterizedType
{
    /**
     * @see #getActualTypeArguments()
     */
    private final Type[] actualTypeArguments;

    /**
     * @see #getOwnerType()
     */
    private final Type ownerType;

    /**
     * @see #getRawType()
     */
    private final Class<?> rawType;

    /**
     * @param ownerType the owner type
     * @param rawType the raw type
     * @param actualTypeArguments the generic arguments
     */
    public DefaultParameterizedType(Type ownerType, Class<?> rawType, Type... actualTypeArguments)
    {
        this.ownerType = ownerType;
        this.actualTypeArguments = actualTypeArguments;
        this.rawType = rawType;
    }

    /**
     * @param type the type to duplicate
     */
    public DefaultParameterizedType(ParameterizedType type)
    {
        this(type.getOwnerType(), (Class<?>) type.getRawType(), type.getActualTypeArguments());
    }

    @Override
    public Type[] getActualTypeArguments()
    {
        return this.actualTypeArguments.clone();
    }

    @Override
    public Type getOwnerType()
    {
        return this.ownerType;
    }

    @Override
    public Type getRawType()
    {
        return this.rawType;
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(this.actualTypeArguments) ^ Objects.hashCode(this.ownerType)
            ^ Objects.hashCode(this.rawType);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof ParameterizedType)) {
            return false;
        }

        ParameterizedType parameterizedType = (ParameterizedType) o;

        return Objects.equals(this.rawType, parameterizedType.getRawType())
            && Objects.equals(this.ownerType, parameterizedType.getOwnerType())
            && Arrays.equals(this.actualTypeArguments, parameterizedType.getActualTypeArguments());
    }

    @Override
    public String toString()
    {
        return ReflectionUtils.serializeType(this);
    }
}
