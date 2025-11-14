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
package org.xwiki.http;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validate {@link URIUtils}.
 * 
 * @version $Id$
 */
class URIUtilsTest
{
    @Test
    void encodePath()
    {
        assertEquals("segment", URIUtils.encodePath("segment"));
        assertEquals("segment/%2B%20%3F%23%26%3D", URIUtils.encodePath("segment/+ ?#&="));
    }

    @Test
    void encodePathSegment()
    {
        assertEquals("segment", URIUtils.encodePath("segment"));
        assertEquals("segment%2F%2B%20%3F%23%26%3D", URIUtils.encodePathSegment("segment/+ ?#&="));
    }

    @Test
    void decode() throws IOException
    {
        assertEquals("segment/+ ?#&=", URIUtils.decode("segment%2F%2B%20%3F%23%26%3D"));
        assertEquals("segment/+ ?#&=", URIUtils.decode("segment%2F%2B+%3F%23%26%3D"));
    }
}
