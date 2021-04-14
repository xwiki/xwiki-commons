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
package org.xwiki.extension.internal.converter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.extension.DefaultExtensionComponent;
import org.xwiki.extension.ExtensionComponent;
import org.xwiki.properties.converter.AbstractConverter;

/**
 * Convert an extension component from a String to an {@link ExtensionComponent} object and the other way around.
 *
 * @version $Id$
 * @since 13.3RC1
 */
@Component
@Singleton
public class ExtensionComponentConverter extends AbstractConverter<ExtensionComponent>
{
    private static int countBackslashes(String str, int index)
    {
        for (int i = index - 1; i >= 0; --i) {
            if (str.charAt(i) != '\\') {
                return index - i - 1;
            }
        }

        return index;
    }

    /**
     * @param values the values to convert
     * @return the list of {@link ExtensionComponent}s created from the passed value
     */
    public static List<ExtensionComponent> toExtensionComponentList(Collection<?> values)
    {
        List<ExtensionComponent> list = new ArrayList<>(values.size());

        for (Object value : values) {
            list.add(toExtensionComponent(value));
        }

        return list;
    }

    /**
     * @param value the value to convert
     * @return the {@link ExtensionComponent} created from the passed value
     */
    public static ExtensionComponent toExtensionComponent(Object value)
    {
        if (value != null) {
            String valueString = value.toString();
            return toExtensionComponent(valueString, valueString.length() - 1);
        }

        return null;
    }

    private static ExtensionComponent toExtensionComponent(String value, int end)
    {
        String valueString = value;

        int index = valueString.indexOf('/');
        String roleType;
        String roleHint;
        if (index > 0 && index < end) {
            int backslashes = countBackslashes(valueString, index);
            if (backslashes > 0) {
                StringBuilder builder = new StringBuilder();
                builder.append(valueString.substring(0, index - backslashes));
                builder.append(StringUtils.repeat('\\', backslashes / 2));
                builder.append(valueString.substring(index));

                valueString = builder.toString();
                index -= backslashes - (backslashes / 2);

                if (backslashes % 2 == 1) {
                    return toExtensionComponent(valueString, index - backslashes - 1);
                }
            }

            roleType = valueString.substring(0, index);
            roleHint = valueString.substring(index + 1);
        } else {
            roleType = valueString;
            roleHint = null;
        }

        return new DefaultExtensionComponent(roleType, roleHint);
    }

    /**
     * @param value the {@link ExtensionComponent} to serialize
     * @return the String version of an {@link ExtensionComponent}
     */
    public static String toString(ExtensionComponent value)
    {
        StringBuilder builder = new StringBuilder();

        builder.append(value.getRoleType());
        if (value.getRoleHint() != null) {
            builder.append('/');
            builder.append(value.getRoleHint());
        }

        return builder.toString();
    }

    /**
     * @param values the list of {@link ExtensionComponent}s to serialize
     * @return the String version of an {@link ExtensionComponent}s list
     */
    public static List<String> toStringList(Collection<ExtensionComponent> values)
    {
        List<String> list = new ArrayList<>(values.size());

        for (ExtensionComponent value : values) {
            list.add(toString(value));
        }

        return list;
    }

    @Override
    protected ExtensionComponent convertToType(Type targetType, Object value)
    {
        return toExtensionComponent(value);
    }

    @Override
    protected String convertToString(ExtensionComponent value)
    {
        return toString(value);
    }
}
