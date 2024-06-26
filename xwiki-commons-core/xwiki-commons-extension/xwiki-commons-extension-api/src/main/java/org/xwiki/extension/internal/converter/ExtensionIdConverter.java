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

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.internal.ExtensionFactory;
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
    @Inject
    private ExtensionFactory factory;

    /**
     * @param values the values to convert
     * @param defaultVersion the default version to set in the {@link ExtensionId} if none can be extracted from the
     *            passed <code>value</code>
     * @return the list of {@link ExtensionId}s created from the passed value
     */
    public static List<ExtensionId> toExtensionIdList(Collection<?> values, Version defaultVersion)
    {
        return toExtensionIdList(values, defaultVersion, null);
    }

    /**
     * @param values the values to convert
     * @param defaultVersion the default version to set in the {@link ExtensionId} if none can be extracted from the
     *            passed <code>value</code>
     * @param factory the factory used to create the {@link Version} instance
     * @return the list of {@link ExtensionId}s created from the passed value
     * @since 9.6
     */
    public static List<ExtensionId> toExtensionIdList(Collection<?> values, Version defaultVersion,
        ExtensionFactory factory)
    {
        List<ExtensionId> list = new ArrayList<>(values.size());

        for (Object value : values) {
            list.add(toExtensionId(value, defaultVersion, factory));
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
        return toExtensionId(value, defaultVersion, null);
    }

    /**
     * @param value the value to convert
     * @param defaultVersion the default version to set in the {@link ExtensionId} if none can be extracted from the
     *            passed <code>value</code>
     * @param factory the factory used to create the {@link Version} instance
     * @return the {@link ExtensionId} created from the passed value
     * @since 9.6
     */
    public static ExtensionId toExtensionId(Object value, Version defaultVersion, ExtensionFactory factory)
    {
        if (value != null) {
            String valueString = value.toString();

            ExtensionConverterParser parser = new ExtensionConverterParser(valueString);

            String id = parser.next(true);
            String versionString = parser.next(false);

            Version version;
            if (versionString != null) {
                version = factory != null ? factory.getVersion(versionString) : new DefaultVersion(versionString);
            } else {
                version = defaultVersion;
            }

            return new ExtensionId(id, version);
        }

        return null;
    }

    /**
     * @param value the {@link ExtensionId} to serialize
     * @return the String version of an {@link ExtensionId}
     */
    public static String toString(ExtensionId value)
    {
        return ExtensionConverterParser.toString(value.getId(), value.getVersion());
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
        return toExtensionId(value, null, this.factory);
    }

    @Override
    protected String convertToString(ExtensionId value)
    {
        return toString(value);
    }
}
