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
import org.xwiki.extension.ExtensionPattern;
import org.xwiki.extension.internal.ExtensionFactory;
import org.xwiki.extension.version.Version;
import org.xwiki.properties.converter.AbstractConverter;

/**
 * Convert from String to a {@link ExtensionPattern} object and the other way around.
 *
 * @version $Id$
 * @since 18.1.0RC1
 * @since 17.10.4
 */
@Component
@Singleton
public class ExtensionPatternConverter extends AbstractConverter<ExtensionPattern>
{
    private static final String PATTERN_ALL = ".*";

    @Inject
    private ExtensionFactory factory;

    /**
     * @param values the values to convert
     * @return the list of {@link ExtensionPattern}s created from the passed value
     */
    public static List<ExtensionPattern> toExtensionPatternList(Collection<?> values)
    {
        return toExtensionPatternList(values, null);
    }

    /**
     * @param values the values to convert
     * @param factory the factory used to create the {@link Version} instance
     * @return the list of {@link ExtensionPattern}s created from the passed value
     */
    public static List<ExtensionPattern> toExtensionPatternList(Collection<?> values, ExtensionFactory factory)
    {
        if (values == null) {
            return null;
        }

        List<ExtensionPattern> list = new ArrayList<>(values.size());

        for (Object value : values) {
            list.add(toExtensionPattern(value, factory));
        }

        return list;
    }

    /**
     * @param value the value to convert
     * @return the {@link ExtensionPattern} created from the passed value
     */
    public static ExtensionPattern toExtensionPattern(Object value)
    {
        return toExtensionPattern(value, null);
    }

    /**
     * @param value the value to convert
     * @param factory the factory used to create the {@link Version} instance
     * @return the {@link ExtensionPattern} created from the passed value
     */
    public static ExtensionPattern toExtensionPattern(Object value, ExtensionFactory factory)
    {
        if (value != null) {
            String pattern = value.toString();

            return ExtensionFactory.getExtensionPattern(factory, pattern);
        }

        return null;
    }

    /**
     * @param value the {@link ExtensionPattern} to serialize
     * @return the String version of an {@link ExtensionPattern}
     */
    public static String toString(ExtensionPattern value)
    {
        if (value != null) {
            if (value.getIdPattern() == null) {
                return PATTERN_ALL;
            }

            return value.getIdPattern().pattern();
        }

        return null;
    }

    /**
     * @param values the list of {@link ExtensionPattern}s to serialize
     * @return the String version of an {@link ExtensionPattern}s list
     */
    public static List<String> toStringList(Collection<ExtensionPattern> values)
    {
        List<String> list = new ArrayList<>(values.size());

        for (ExtensionPattern value : values) {
            list.add(toString(value));
        }

        return list;
    }

    @Override
    protected ExtensionPattern convertToType(Type targetType, Object value)
    {
        return toExtensionPattern(value, this.factory);
    }

    @Override
    protected String convertToString(ExtensionPattern value)
    {
        return toString(value);
    }
}
