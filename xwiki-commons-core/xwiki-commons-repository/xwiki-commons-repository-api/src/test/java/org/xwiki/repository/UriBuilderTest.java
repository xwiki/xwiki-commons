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
package org.xwiki.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Validate {@link UriBuilder}.
 *
 * @version $Id$
 */
public class UriBuilderTest
{
    @Test
    void buildWithTwoElements()
    {
        UriBuilder uriBuilder = new UriBuilder("http://base", "{key}/{key2}");

        assertEquals("http://base/1/2", uriBuilder.build("1", "2").toString());
    }

    @Test
    void buildWithNoElements()
    {
        UriBuilder uriBuilder = new UriBuilder("http://base", "");

        assertEquals("http://base/", uriBuilder.build().toString());
    }

    @Test
    void buildWithParam()
    {
        UriBuilder uriBuilder = new UriBuilder("http://base", "element/");

        uriBuilder.queryParam("param", "value");

        assertEquals("http://base/element/?param=value", uriBuilder.build().toString());
    }

    @Test
    void buildWithcompleteBase()
    {
        UriBuilder uriBuilder = new UriBuilder("http://host:1111/base?baseparam=basevalue#basefragment", "element/");

        uriBuilder.queryParam("param", "value");

        assertEquals("http://host:1111/base/element/?baseparam=basevalue&param=value#basefragment", uriBuilder
            .build().toString());
    }

    @Test
    void buildWithUTF8()
    {
        UriBuilder uriBuilder = new UriBuilder("http://base", "{key}");

        uriBuilder.queryParam("param", "\u00e9");

        assertEquals("http://base/%C3%A9?param=%C3%A9", uriBuilder.build("\u00e9").toString());
    }

    @Test
    void testClone()
    {
        UriBuilder uriBuilder = new UriBuilder("http://base", "{key}");

        UriBuilder clonedUriBuilder = uriBuilder.clone();

        assertNotNull(clonedUriBuilder);
    }
}
