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
package org.xwiki.properties.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.xwiki.properties.PropertyDescriptor;

/**
 * Default implementation for {@link PropertyDescriptor}.
 *
 * @version $Id$
 * @since 2.0M2
 */
public class BackwardCompatiblePropertyDescriptor implements PropertyDescriptor
{
    private PropertyDescriptor propertyDescriptor;

    /**
     * @param propertyDescriptor property desriptor on which the methods are called
     */
    public BackwardCompatiblePropertyDescriptor(PropertyDescriptor propertyDescriptor)
    {
        this.propertyDescriptor = propertyDescriptor;
    }

    @Override
    public String getId()
    {
        return propertyDescriptor.getId();
    }

    @Override
    public String getName()
    {
        return propertyDescriptor.getName();
    }

    @Override
    public String getDescription()
    {
        return propertyDescriptor.getDescription();
    }

    @Override
    public Class<?> getPropertyClass()
    {
        return propertyDescriptor.getPropertyClass();
    }

    @Override
    public Type getPropertyType()
    {
        return propertyDescriptor.getPropertyType();
    }

    @Override
    public Object getDefaultValue()
    {
        return propertyDescriptor.getDefaultValue();
    }

    @Override
    public boolean isMandatory()
    {
        return propertyDescriptor.isMandatory();
    }

    @Override
    public Field getFied()
    {
        return propertyDescriptor.getFied();
    }

    @Override
    public Field getField()
    {
        return propertyDescriptor.getField();
    }

    @Override
    public Method getReadMethod()
    {
        return propertyDescriptor.getReadMethod();
    }

    @Override
    public Method getWriteMethod()
    {
        return propertyDescriptor.getWriteMethod();
    }
}
