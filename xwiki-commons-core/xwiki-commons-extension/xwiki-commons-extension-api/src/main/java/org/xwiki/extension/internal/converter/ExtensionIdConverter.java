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
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.internal.DefaultVersion;
import org.xwiki.properties.converter.AbstractConverter;

/**
 * Convert an extension feature from a String to a {@link ExtensionId} object and the other way around.
 *
 * @version $Id$
 * @since 8.0M1
 */
@Component
@Singleton
public class ExtensionIdConverter extends AbstractConverter<ExtensionId>
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
     * @param defaultVersion the default version to set in the {@link ExtensionId} if none can be extracted from the
     *            passed <code>value</code>
     * @return the list of {@link ExtensionId}s created from the passed value
     */
    public static List<ExtensionId> toExtensionIdList(Collection<?> values, Version defaultVersion)
    {
        List<ExtensionId> list = new ArrayList<>(values.size());

        for (Object value : values) {
            list.add(toExtensionId(value, defaultVersion));
        }

        return list;
    }

    /**
     * @param value the value to convert
     * @param defaultVersion the default version to set in the {@link ExtensionId} if none can be extracted from the
     *            passed <code>value</code>
     * @return the {@link ExtensionId} created from the passed value
     */
    public static ExtensionId toExtensionId(Object value, Version defaultVersion)
    {
        if (value != null) {
            String valueString = value.toString();
            return toExtensionId(valueString, valueString.length() - 1, defaultVersion);
        }

        return null;
    }

    private static ExtensionId toExtensionId(String value, int end, Version defaultVersion)
    {
        String valueString = value;

        int index = valueString.lastIndexOf('/');
        String id = valueString;
        Version version;
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
                    return toExtensionId(valueString, index - backslashes - 1, defaultVersion);
                }
            }

            id = valueString.substring(0, index);
            version = new DefaultVersion(valueString.substring(index + 1));
        } else {
            id = valueString;
            version = defaultVersion;
        }

        return new ExtensionId(id, version);
    }

    /**
     * @param value the {@link ExtensionId} to serialize
     * @return the String version of an {@link ExtensionId}
     */
    public static String toString(ExtensionId value)
    {
        StringBuilder builder = new StringBuilder();

        builder.append(value.getId());

        if (value.getVersion() != null) {
            builder.append('/');
            builder.append(value.getVersion());
        }

        return builder.toString();
    }

    /**
     * @param values the list of {@link ExtensionId}s to serialize
     * @return the String version of an {@link ExtensionId}s list
     */
    public static List<String> toStringList(Collection<ExtensionId> values)
    {
        List<String> list = new ArrayList<>(values.size());

        for (ExtensionId value : values) {
            list.add(toString(value));
        }

        return list;
    }

    @Override
    protected ExtensionId convertToType(Type targetType, Object value)
    {
        return toExtensionId(value, null);
    }

    @Override
    protected String convertToString(ExtensionId value)
    {
        return toString(value);
    }
}
