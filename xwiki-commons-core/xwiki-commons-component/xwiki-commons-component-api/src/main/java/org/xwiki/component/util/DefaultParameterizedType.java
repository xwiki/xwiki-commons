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
        return Arrays.hashCode(this.actualTypeArguments) ^ ObjectUtils.hasCode(this.ownerType)
            ^ ObjectUtils.hasCode(this.rawType);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof ParameterizedType)) {
            return false;
        }

        ParameterizedType parameterizedType = (ParameterizedType) o;

        return ObjectUtils.equals(this.rawType, parameterizedType.getRawType())
            && ObjectUtils.equals(this.ownerType, parameterizedType.getOwnerType())
            && Arrays.equals(this.actualTypeArguments, parameterizedType.getActualTypeArguments());
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (this.ownerType != null) {
            if (this.ownerType instanceof Class) {
                sb.append(((Class<?>) this.ownerType).getName());
            } else {
                sb.append(this.ownerType.toString());
            }

            sb.append('.');

            if (this.ownerType instanceof ParameterizedType) {
                // Find simple name of nested type by removing the
                // shared prefix with owner.
                sb.append(this.rawType.getName().replace(
                    ((Class<?>) ((ParameterizedType) this.ownerType).getRawType()).getName() + '$', ""));
            } else {
                sb.append(this.rawType.getName());
            }
        } else {
            sb.append(this.rawType.getName());
        }

        if (this.actualTypeArguments != null && this.actualTypeArguments.length > 0) {
            sb.append("< ");

            boolean first = true;
            for (Type type : this.actualTypeArguments) {
                if (!first) {
                    sb.append(", ");
                }

                if (type instanceof Class) {
                    sb.append(((Class<?>) type).getName());
                } else {
                    sb.append(type.toString());
                }

                first = false;
            }

            sb.append(" >");
        }

        return sb.toString();
    }
}
