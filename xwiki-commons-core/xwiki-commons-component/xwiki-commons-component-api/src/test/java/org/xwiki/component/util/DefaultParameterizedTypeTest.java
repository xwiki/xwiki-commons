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
package org.xwiki.component.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for {@link DefaultParameterizedType}.
 *
 * @version $Id$
 */
public class DefaultParameterizedTypeTest
{
    @Test
    public void toStringTest()
    {
        DefaultParameterizedType type = new DefaultParameterizedType(null, Void.class);
        assertEquals("java.lang.Void", type.toString());

        type = new DefaultParameterizedType(Collections.class, List.class);
        assertEquals("java.util.Collections.java.util.List", type.toString());

        type = new DefaultParameterizedType(Collections.class, List.class, String.class);
        assertEquals("java.util.Collections.java.util.List<java.lang.String>", type.toString());

        type = new DefaultParameterizedType(null, Map.class, Integer.class, String.class);
        assertEquals("java.util.Map<java.lang.Integer, java.lang.String>", type.toString());

        type = new DefaultParameterizedType(null, Map.class, Integer.class,
            new DefaultParameterizedType(null, List.class, String.class));
        assertEquals("java.util.Map<java.lang.Integer, java.util.List<java.lang.String>>", type.toString());
    }
}
