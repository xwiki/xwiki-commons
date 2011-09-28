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
package org.xwiki.component.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * Provides useful methods for implementing {@link ComponentDependencyFactory}.
 *
 * @version $Id$
 * @since 3.2RC1
 */
public abstract class AbstractComponentDependencyFactory implements ComponentDependencyFactory
{
    /**
     * Extract component role from the field to inject.
     *
     * @param field the field to inject
     * @return the role of the field to inject
     */
    protected Class<?> getFieldRole(Field field)
    {
        Class<?> role;

        // Handle case of list or map
        if (isDependencyOfListType(field.getType())) {
            role = getGenericRole(field);
        } else {
            role = field.getType();
        }

        return role;
    }

    /**
     * @param type the type for which to verify if it's a list or not
     * @return true if the type is a list (Collection or Map), false otherwise
     */
    protected boolean isDependencyOfListType(Class<?> type)
    {
        return Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type);
    }

    /**
     * Extract generic type from the list field.
     *
     * @param field the list field to inject
     * @return the role of the components in the list
     */
    protected Class<?> getGenericRole(Field field)
    {
        Type type = field.getGenericType();

        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Type[] types = pType.getActualTypeArguments();
            if (types.length > 0 && types[types.length - 1] instanceof Class) {
                return (Class) types[types.length - 1];
            }
        }

        return null;
    }
}
