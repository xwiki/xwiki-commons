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
package org.xwiki.job.internal.xstream;

import java.util.List;

import org.apache.commons.lang3.ClassUtils;
import org.xwiki.component.annotation.Role;

/**
 * Various XStream related utilities.
 *
 * @version $Id$
 * @since 5.4M1
 */
public final class XStreamUtils
{
    private XStreamUtils()
    {

    }

    /**
     * @param obj the value to check
     * @return true if the type serialization cannot fail
     */
    public static boolean isSafeType(Object obj)
    {
        return obj == null || obj instanceof String || obj instanceof Number || obj.getClass().isArray()
            || obj instanceof Enum;
    }

    /**
     * @param item the item to serialize
     * @return true of the item looks like a component
     */
    public static boolean isComponent(Object item)
    {
        if (item != null) {
            List<Class<?>> interfaces = ClassUtils.getAllInterfaces(item.getClass());

            for (Class<?> iface : interfaces) {
                if (iface.isAnnotationPresent(Role.class)) {
                    return true;
                }
            }
        }

        return false;
    }
}
