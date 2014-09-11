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
import org.xwiki.properties.converter.AbstractConverter;
import org.xwiki.properties.converter.ConversionException;

/**
 * Converter that converts a value into an enumeration class value.
 *
 * @version $Id$
 * @since 5.2M1
 */
@Component
@Singleton
public class EnumConverter extends AbstractConverter<Enum>
{
    @Override
    protected <E extends Enum> E convertToType(Type type, Object value)
    {
        if (value != null) {
            Object[] enumValues = ((Class<?>) type).getEnumConstants();

            String testValue = value.toString();
            for (Object enumValue : enumValues) {
                // Conversion is case insensitive
                if (enumValue.toString().equalsIgnoreCase(testValue)) {
                    return (E) enumValue;
                }
            }

            throw new ConversionException(generateInvalidErrorMessage(enumValues, testValue));
        } else {
            return null;
        }
    }

    /**
     * Generate error message to use in the {@link ConversionException}.
     *
     * @param enumValues possible values of the enum.
     * @param testValue the value to convert.
     * @return the generated error message.
     */
    private String generateInvalidErrorMessage(Object[] enumValues, String testValue)
    {
        StringBuffer errorMessage = new StringBuffer("Unable to convert value [" + testValue + "].");

        errorMessage.append(" Allowed values are (case insensitive) ");

        StringBuffer valueList = new StringBuffer();

        int index = 1;
        for (Object enumValue : enumValues) {
            if (valueList.length() > 0) {
                if (++index == enumValues.length) {
                    valueList.append(" or ");
                } else {
                    valueList.append(", ");
                }
            }

            valueList.append('"');
            valueList.append(enumValue);
            valueList.append('"');
        }

        errorMessage.append(valueList);
        errorMessage.append('.');

        return errorMessage.toString();
    }
}
