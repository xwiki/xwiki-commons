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
package org.xwiki.filter.xml.internal;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Various tools.
 *
 * @version $Id$
 * @since 5.2M1
 */
public final class XMLUtils
{
    /**
     * An index based parameter.
     */
    public static final Pattern INDEX_PATTERN = Pattern.compile("_(\\d+)");

    /**
     * The default mapping between interface and instance.
     */
    private static final Map<Class<?>, Object> DEFAULTS = new HashMap<>();

    static {
        DEFAULTS.put(boolean.class, false);
        DEFAULTS.put(char.class, '\0');
        DEFAULTS.put(byte.class, (byte) 0);
        DEFAULTS.put(short.class, (short) 0);
        DEFAULTS.put(int.class, 0);
        DEFAULTS.put(long.class, 0L);
        DEFAULTS.put(float.class, 0f);
        DEFAULTS.put(double.class, 0d);
        DEFAULTS.put(Map.class, new LinkedHashMap<Object, Object>());
        DEFAULTS.put(Set.class, new LinkedHashSet<Object>());
        DEFAULTS.put(List.class, new ArrayList<Object>());
        DEFAULTS.put(Collection.class, new ArrayList<Object>());
        DEFAULTS.put(Locale.class, Locale.ROOT);
        DEFAULTS.put(URI.class, null);
        DEFAULTS.put(URL.class, null);
    }

    /**
     * The classes of object that can easily be converted to simple String.
     */
    private static final Set<Class<?>> SIMPLECLASSES = new HashSet<>(Arrays.<Class<?>>asList(String.class,
        Character.class, Boolean.class, byte[].class, Locale.class, URL.class, URI.class));

    /**
     * Utility class.
     */
    private XMLUtils()
    {
    }

    /**
     * @param type the type can be converted to simple String
     * @return true of the type is simple
     */
    public static boolean isSimpleType(Type type)
    {
        boolean simpleType = false;

        if (type instanceof Class) {
            Class<?> typeClass = (Class<?>) type;

            simpleType = SIMPLECLASSES.contains(typeClass) || Number.class.isAssignableFrom(typeClass)
                || typeClass.isPrimitive() || typeClass.isEnum();
        }

        return simpleType;
    }

    /**
     * @param type the type
     * @return the default value of the provided type
     */
    public static Object emptyValue(Class<?> type)
    {
        Object defaultValue = null;

        if (DEFAULTS.containsKey(type)) {
            defaultValue = DEFAULTS.get(type);
        } else {
            try {
                defaultValue = type.newInstance();
            } catch (Exception e) {
                // Ignore
            }
        }

        return defaultValue;
    }
}
