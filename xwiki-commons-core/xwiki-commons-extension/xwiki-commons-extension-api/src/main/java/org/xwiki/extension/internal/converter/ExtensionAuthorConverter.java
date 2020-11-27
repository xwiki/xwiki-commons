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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionAuthor;
import org.xwiki.extension.internal.ExtensionFactory;
import org.xwiki.extension.version.Version;
import org.xwiki.properties.converter.AbstractConverter;

/**
 * Convert an extension feature from a String to an {@link ExtensionAuthor} object and the other way around.
 *
 * @version $Id$
 * @since 12.9RC1
 */
@Component
@Singleton
public class ExtensionAuthorConverter extends AbstractConverter<ExtensionAuthor>
{
    @Inject
    private ExtensionFactory factory;

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
     * @return the list of {@link ExtensionAuthor}s created from the passed value
     */
    public static List<ExtensionAuthor> toExtensionAuthorList(Collection<?> values)
    {
        return toExtensionAuthorList(values, null);
    }

    /**
     * @param values the values to convert
     * @param factory the factory used to create the {@link Version} instance
     * @return the list of {@link ExtensionAuthor}s created from the passed value
     * @since 9.6
     */
    public static List<ExtensionAuthor> toExtensionAuthorList(Collection<?> values, ExtensionFactory factory)
    {
        List<ExtensionAuthor> list = new ArrayList<>(values.size());

        for (Object value : values) {
            list.add(toExtensionAuthor(value, factory));
        }

        return list;
    }

    /**
     * @param value the value to convert
     * @return the {@link ExtensionAuthor} created from the passed value
     */
    public static ExtensionAuthor toExtensionAuthor(Object value)
    {
        return toExtensionAuthor(value, null);
    }

    /**
     * @param value the value to convert
     * @param factory the factory used to create the {@link Version} instance
     * @return the {@link ExtensionAuthor} created from the passed value
     */
    public static ExtensionAuthor toExtensionAuthor(Object value, ExtensionFactory factory)
    {
        if (value != null) {
            String valueString = value.toString();
            return toExtensionAuthor(valueString, valueString.length() - 1, factory);
        }

        return null;
    }

    private static ExtensionAuthor toExtensionAuthor(String value, int end, ExtensionFactory factory)
    {
        String valueString = value;

        int index = valueString.lastIndexOf('/');
        String name;
        String url;
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
                    return toExtensionAuthor(valueString, index - backslashes - 1, factory);
                }
            }

            name = valueString.substring(0, index);
            url = valueString.substring(index + 1);
        } else {
            name = valueString;
            url = null;
        }

        return factory.getExtensionAuthor(name, url);
    }

    /**
     * @param value the {@link ExtensionAuthor} to serialize
     * @return the String version of an {@link ExtensionAuthor}
     */
    public static String toString(ExtensionAuthor value)
    {
        StringBuilder builder = new StringBuilder();

        if (value.getName() != null) {
            builder.append(value.getName().replace("\\", "\\\\").replace("/", "\\/"));
        }

        if (value.getURLString() != null) {
            builder.append('/');
            builder.append(value.getURLString());
        }

        return builder.toString();
    }

    /**
     * @param values the list of {@link ExtensionAuthor}s to serialize
     * @return the String version of an {@link ExtensionAuthor}s list
     */
    public static List<String> toStringList(Collection<ExtensionAuthor> values)
    {
        List<String> list = new ArrayList<>(values.size());

        for (ExtensionAuthor value : values) {
            list.add(toString(value));
        }

        return list;
    }

    @Override
    protected ExtensionAuthor convertToType(Type targetType, Object value)
    {
        return toExtensionAuthor(value, this.factory);
    }

    @Override
    protected String convertToString(ExtensionAuthor value)
    {
        return toString(value);
    }
}
