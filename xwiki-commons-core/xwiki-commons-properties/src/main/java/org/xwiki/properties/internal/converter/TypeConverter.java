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
package org.xwiki.properties.internal.converter;

import java.lang.reflect.Type;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.properties.converter.AbstractConverter;
import org.xwiki.properties.converter.ConversionException;

/**
 * Converter that converts a value into an {@link Type} object.
 *
 * @version $Id$
 * @since 6.1M1
 */
@Component
@Singleton
public class TypeConverter extends AbstractConverter<Type>
{
    @Override
    protected Type convertToType(Type type, Object value)
    {
        Type result = null;
        if (value != null) {
            String valueString = value.toString();
            try {
                result = ReflectionUtils.unserializeType(valueString, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                throw new ConversionException("Invalid Type [" + valueString + "]", e);
            }
        }

        return result;
    }

    @Override
    protected String convertToString(Type type)
    {
        return ReflectionUtils.serializeType(type);
    }
}
