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
package org.xwiki.properties;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.xwiki.stability.Unstable;

/**
 * Describe a property in a bean.
 *
 * @version $Id$
 * @since 2.0M2
 */
public interface PropertyDescriptor
{
    /**
     * @return the identifier of the property.
     * @since 2.1M1
     */
    String getId();

    /**
     * @return the display name of the property.
     * @since 2.1M1
     */
    String getName();

    /**
     * @return the description of the property.
     */
    String getDescription();

    /**
     * @return the type of the property.
     * @deprecated since 3.0M1 use {@link #getPropertyType()} instead
     */
    @Deprecated
    Class<?> getPropertyClass();

    /**
     * @return the type of the property.
     * @since 3.0M1
     */
    Type getPropertyType();

    /**
     * @return the default value of the property.
     */
    Object getDefaultValue();

    /**
     * @return indicate if the property is mandatory.
     */
    boolean isMandatory();

    /**
     * @return the read method. If null it generally mean that the property is a public field.
     */
    Method getReadMethod();

    /**
     * @return the write method. If null it generally mean that the property is a public field.
     */
    Method getWriteMethod();

    /**
     * @return the field. If null if generally mean that the property is based on getter/setter.
     * @deprecated since 4.2M1 use {@link #getField()} instead
     */
    @Deprecated
    Field getFied();

    /**
     * @return the field. If null if generally mean that the property is based on getter/setter.
     * @since 4.2M1
     */
    Field getField();

    /**
     * @return indicates if the property is deprecated.
     * @since 10.10RC1
     */
    default boolean isDeprecated() {
        return false;
    }

    /**
     * @return indicates if the property is advanced.
     * @since 10.10RC1
     */
    default boolean isAdvanced() {
        return false;
    }

    /**
     * @return the hierarchy of groups.
     * @since 10.11RC1
     */
    default PropertyGroupDescriptor getGroupDescriptor() {
        return new PropertyGroupDescriptor(null);
    }

    /**
     * @return the type used when displaying the property.
     * @since 11.0
     */
    @Unstable
    default Type getDisplayType()
    {
        return getPropertyType();
    }
}
