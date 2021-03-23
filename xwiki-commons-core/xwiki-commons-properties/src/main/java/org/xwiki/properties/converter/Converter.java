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
package org.xwiki.properties.converter;

import java.lang.reflect.Type;

import org.xwiki.component.annotation.Role;
import org.xwiki.properties.BeanManager;
import org.xwiki.properties.ConverterManager;

/**
 * General purpose data type converter that can be registered and used within the frameworks like
 * {@link ConverterManager} and {@link BeanManager} package to manage the conversion of objects from one type to
 * another.
 * <p>
 * The type supported by the converted is indicated in its role.
 * <p>
 * For example:
 * <p>
 * <blockquote>
 *
 * <pre>
 * &#064;Component
 * public class IntegerConverter implements Converter&lt;java.lang.Integer&gt;
 * </pre>
 *
 * </blockquote>
 * <p>
 * If a conversion cannot be performed successfully the converter is expected to throw a
 * {@link org.xwiki.properties.converter.ConversionException}.
 *
 * @param <T> the type in which the provided value has to be converted
 * @version $Id$
 * @since 2.0M2
 */
@Role
public interface Converter<T>
{
    /**
     * Convert the specified input object into an output object of the specified type.
     * 
     * @param <G> the type in which the provided value has to be converted
     * @param targetType the type in which the provided value has to be converted
     * @param sourceValue the value to convert
     * @return the converted value
     * @throws org.xwiki.properties.converter.ConversionException if conversion cannot be performed successfully
     * @since 3.0M1
     */
    <G> G convert(Type targetType, Object sourceValue);
}
