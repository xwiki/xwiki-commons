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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.properties.ConverterManager;
import org.xwiki.properties.converter.AbstractConverter;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.stability.Unstable;

/**
 * Converter for {@link Optional} type, supporting:
 * <ul>
 * <li>{@code X} to {@code Optional<Y>}, if conversion from X to Y is possible</li>
 * <li>{@code Optional<X>} to {@code Optional<Y>}, if conversion from X to Y is possible.</li>
 * </ul>
 * 
 * @version $Id$
 * @since 18.1.0RC1
 */
@Component
@Singleton
@Unstable
public class OptionalConverter extends AbstractConverter<Optional>
{
    @Inject
    private ConverterManager converterManager;

    @Override
    @SuppressWarnings("unchecked")
    protected <G extends Optional> G convertToType(Type targetType, Object value)
    {
        if (targetType instanceof ParameterizedType parameterizedTargetType
            && Optional.class.equals(parameterizedTargetType.getRawType())) {
            if (value instanceof Optional optionalValue) {
                // Convert Optional<X> to Optional<Y>, if conversion from X to Y is possible.
                Type targetOptionalValueType = parameterizedTargetType.getActualTypeArguments()[0];
                return (G) optionalValue
                    .map(nonNullValue -> this.converterManager.convert(targetOptionalValueType, nonNullValue));
            } else {
                // Convert X to Optional<Y>, if conversion from X to Y is possible.
                Type optionalValueType = parameterizedTargetType.getActualTypeArguments()[0];
                Object convertedValue = value == null ? null : this.converterManager.convert(optionalValueType, value);
                return (G) Optional.ofNullable(convertedValue);
            }
        }

        throw new ConversionException(
            String.format("The target type [%s] is not supported by this converter.", targetType));

    }

    @Override
    @SuppressWarnings("unchecked")
    protected String convertToString(Optional value)
    {
        return (String) value.map(nonNullValue -> this.converterManager.convert(String.class, nonNullValue))
            .orElse(null);
    }
}
