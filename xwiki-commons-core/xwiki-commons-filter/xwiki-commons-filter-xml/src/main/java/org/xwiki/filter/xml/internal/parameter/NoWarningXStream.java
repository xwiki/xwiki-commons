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
package org.xwiki.filter.xml.internal.parameter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;

/**
 * Workaround for XStream security rules warning.
 *
 * @version $Id$
 * @since 9.5.2
 * @since 9.6RC1
 */
public class NoWarningXStream extends XStream
{
    /**
     * Constructs an XStream with a special {@link HierarchicalStreamDriver}.
     * <p>
     * The instance will tries to determine the best match for the {@link ReflectionProvider} on its own.
     * </p>
     *
     * @param hierarchicalStreamDriver the driver instance
     */
    public NoWarningXStream(HierarchicalStreamDriver hierarchicalStreamDriver)
    {
        super(hierarchicalStreamDriver);

        // Allow everything since using a white list is totally unusable for job serialization use case where we don't
        // know the types in advance (we don't even know the ClassLoader in advance...).
        addPermission(c -> true);
    }
}
