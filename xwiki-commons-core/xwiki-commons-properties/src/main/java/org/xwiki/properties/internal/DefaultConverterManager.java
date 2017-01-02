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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.properties.ConverterManager;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.properties.converter.Converter;

/**
 * Default implementation for {@link ConverterManager}.
 * <p>
 * It try to find a {@link Converter} for the provided target type. If it can't find:
 * <ul>
 * <li>if the type is an {@link Enum}, it use the {@link Converter} with component hint "enum"</li>
 * <li>then it use the default {@link Converter} (which is based on {@link org.apache.commons.beanutils.ConvertUtils} by
 * default)</li>
 * </ul>
 *
 * @version $Id$
 * @since 2.0M2
 */
@Component
@Singleton
public class DefaultConverterManager implements ConverterManager
{
    /**
     * Use to find the proper {@link Converter} component for provided target type.
     */
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    /**
     * Used when no direct {@link Converter} can be found for provided target type and the target type is an
     * {@link Enum}.
     */
    @Inject
    private Converter<Enum> enumConverter;

    /**
     * Used when no direct {@link Converter} can be found for provided target type.
     */
    @Inject
    private Converter defaultConverter;

    /**
     * The logger to use for logging.
     */
    @Inject
    private Logger logger;

    @Override
    public <T> T convert(Type targetType, Object value)
    {
        // If the value is already of the right type return it
        if (targetType instanceof Class && ((Class<?>) targetType).isInstance(value)) {
            return (T) value;
        }

        // Handle wildcards (<? extends OtherType>)
        if (targetType instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) targetType).getUpperBounds();

            if (upperBounds.length == 0) {
                // If the asked type is <?>, don't convert anything
                return (T) value;
            } else {
                return convert(upperBounds[0], value);
            }
        }

        // Converter type
        Type converterType = targetType;
        if (converterType == String.class && value != null) {
            converterType = value.getClass();
        }

        // Convert
        Converter<T> converter = lookupConverter(converterType);

        if (converter != null) {
            return converter.convert(targetType, value);
        } else {
            throw new ConversionException(
                "Cannot find Converter to convert value [" + value + "] to type [" + targetType + "] ");
        }
    }

    /**
     * Find the right {@link Converter} for the provided {@link Class}.
     *
     * @param <T> the type in which the provided value has to be converted
     * @param targetType the type to convert to
     * @return the {@link Converter} corresponding to the class
     */
    private <T> Converter<T> lookupConverter(Type targetType)
    {
        // Try with complete type
        Converter<T> converter = getConverter(targetType);

        // Try with simple class
        if (converter == null && targetType instanceof ParameterizedType) {
            Class<?> targetClass = ReflectionUtils.getTypeClass(targetType);
            converter = getConverter(targetClass);
        }

        if (converter == null) {
            if (targetType instanceof Class && Enum.class.isAssignableFrom((Class<?>) targetType)) {
                // It's an Enum
                converter = (Converter<T>) this.enumConverter;
            } else {
                // Fallback on default converter

                this.logger.debug("Using the default Converter for type [{}]", targetType);

                converter = this.defaultConverter;
            }
        }

        return converter;
    }

    @Override
    public <T> Converter<T> getConverter(Type targetType)
    {
        try {
            ComponentManager componentManager = this.componentManagerProvider.get();

            ParameterizedType converterType = new DefaultParameterizedType(null, Converter.class, targetType);
            if (componentManager.hasComponent(converterType)) {
                return componentManager.getInstance(converterType);
            }

            // Old way of registering converters
            String typeGenericName = getTypeGenericName(targetType);
            if (componentManager.hasComponent(Converter.class, typeGenericName)) {
                return componentManager.getInstance(Converter.class, typeGenericName);
            }
        } catch (ComponentLookupException e) {
            throw new ConversionException("Failed to initialize converter for target type [" + targetType + "]", e);
        }

        return null;
    }

    /**
     * Get class name without generics.
     *
     * @param type the type
     * @return type name without generics
     */
    private String getTypeName(Type type)
    {
        String name;
        if (type instanceof Class) {
            name = ((Class<?>) type).getName();
        } else if (type instanceof ParameterizedType) {
            name = ((Class<?>) ((ParameterizedType) type).getRawType()).getName();
        } else {
            name = type.toString();
        }

        return name;
    }

    /**
     * Get type name.
     *
     * @param type the type
     * @return type name
     */
    private String getTypeGenericName(Type type)
    {
        StringBuilder sb = new StringBuilder(getTypeName(type));

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            Type[] generics = parameterizedType.getActualTypeArguments();
            if (generics.length > 0) {
                sb.append('<');
                for (int i = 0; i < generics.length; ++i) {
                    if (i > 0) {
                        sb.append(',');
                    }
                    sb.append(getTypeGenericName(generics[i]));
                }
                sb.append('>');
            }
        }

        return sb.toString();
    }
}
