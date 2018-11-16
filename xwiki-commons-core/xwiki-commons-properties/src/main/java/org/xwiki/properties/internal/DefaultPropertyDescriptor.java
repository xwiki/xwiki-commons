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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.xwiki.properties.PropertyDescriptor;
import org.xwiki.properties.PropertyGroupDescriptor;

/**
 * Default implementation for {@link PropertyDescriptor}.
 *
 * @version $Id$
 * @since 2.0M2
 */
public class DefaultPropertyDescriptor implements PropertyDescriptor
{
    /**
     * @see #getId()
     * @since 2.1M1
     */
    private String id;

    /**
     * @see #getName()
     * @since 2.1M1
     */
    private String name;

    /**
     * @see #getDescription()
     */
    private String description;

    /**
     * @see #getPropertyType()
     */
    private Type propertyType;

    /**
     * @see #getDefaultValue()
     */
    private Object defaultValue;

    /**
     * @see #isMandatory()
     */
    private boolean mandatory;

    /**
     * @see #getFied()
     */
    private Field field;

    /**
     * @see #getReadMethod()
     */
    private Method readMethod;

    /**
     * @see #getWriteMethod()
     */
    private Method writeMethod;

    /**
     * @see #isDeprecated()
     */
    private boolean deprecated;

    /**
     * @see #isAdvanced()
     */
    private boolean advanced;

    /**
     * @see #getGroupDescriptor()
     */
    private PropertyGroupDescriptor group;

    @Override
    public String getId()
    {
        return this.id;
    }

    /**
     * @param id the identifier of the property.
     * @see #getId()
     * @since 2.1M1
     */
    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name the display name of the property.
     * @see #getName()
     * @since 2.1M1
     */
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @param description the description of the property.
     * @see #getDescription()
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    @Deprecated
    public Class<?> getPropertyClass()
    {
        Class<?> clazz;
        if (this.propertyType instanceof Class) {
            clazz = (Class) this.propertyType;
        } else if (this.propertyType instanceof ParameterizedType) {
            clazz = (Class) ((ParameterizedType) this.propertyType).getRawType();
        } else {
            clazz = null;
        }

        return clazz;
    }

    @Override
    public Type getPropertyType()
    {
        return this.propertyType;
    }

    /**
     * @param propertyType the class of the property.
     * @see #getPropertyClass()
     */
    public void setPropertyType(Type propertyType)
    {
        this.propertyType = propertyType;
    }

    @Override
    public Object getDefaultValue()
    {
        return this.defaultValue;
    }

    /**
     * @param defaultValue the default value of the property.
     * @see #getDefaultValue()
     */
    public void setDefaultValue(Object defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean isMandatory()
    {
        return this.mandatory;
    }

    /**
     * @param mandatory indicate if the property is mandatory.
     * @see #isMandatory()
     */
    public void setMandatory(boolean mandatory)
    {
        this.mandatory = mandatory;
    }

    /**
     * @see org.xwiki.properties.PropertyDescriptor#getFied().
     * @param field the {@link Field}.
     */
    public void setField(Field field)
    {
        this.field = field;
    }

    @Override
    @Deprecated
    public Field getFied()
    {
        return getField();
    }

    @Override
    public Field getField()
    {
        return this.field;
    }

    /**
     * @see org.xwiki.properties.PropertyDescriptor#getReadMethod().
     * @param readMethod the read {@link Method}.
     */
    public void setReadMethod(Method readMethod)
    {
        this.readMethod = readMethod;
    }

    @Override
    public Method getReadMethod()
    {
        return this.readMethod;
    }

    /**
     * @see org.xwiki.properties.PropertyDescriptor#getWriteMethod().
     * @param writeMethod the write {@link Method}.
     */
    public void setWriteMethod(Method writeMethod)
    {
        this.writeMethod = writeMethod;
    }

    @Override
    public Method getWriteMethod()
    {
        return this.writeMethod;
    }

    @Override
    public boolean isDeprecated()
    {
        return this.deprecated;
    }

    /**
     * @param deprecated indicates if the parameter is deprecated
     * @see #isDeprecated()
     * @since 10.10RC1
     */
    public void setDeprecated(boolean deprecated)
    {
        this.deprecated = deprecated;
    }

    @Override
    public boolean isAdvanced()
    {
        return this.advanced;
    }

    /**
     * @param advanced indicates if the parameter is advanced
     * @see #isAdvanced()
     * @since 10.10RC1
     */
    public void setAdvanced(boolean advanced)
    {
        this.advanced = advanced;
    }

    @Override
    public PropertyGroupDescriptor getGroupDescriptor()
    {
        return this.group;
    }

    /**
     * @param group the property hierarchy of groups
     * @see #getGroupDescriptor()
     */
    public void setGroup(PropertyGroupDescriptor group)
    {
        this.group = group;
    }
}
