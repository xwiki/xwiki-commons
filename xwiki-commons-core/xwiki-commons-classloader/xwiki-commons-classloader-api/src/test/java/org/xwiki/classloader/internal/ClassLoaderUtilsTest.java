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
package org.xwiki.classloader.internal;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Validate {@link ClassLoaderUtils}.
 * 
 * @version $Id$
 */
class ClassLoaderUtilsTest
{
    private static final String PREFIX = "prefix/";

    private static final String RESOURCE_NAME = "resource";

    private static final String PREFIXED_RESOURCE_NAME = PREFIX + RESOURCE_NAME;

    private static final String RESOURCE_NAME_BACK = "folder/../" + RESOURCE_NAME;

    private static final String PREFIXED_RESOURCE_NAME_BACK = PREFIX + RESOURCE_NAME_BACK;

    private ClassLoader classLoader;

    private URL resouceURL;

    private URL prefixedResourceURL;

    private InputStream inputStream;

    private InputStream prefixedInputStream;

    @BeforeEach
    void beforeEach() throws MalformedURLException
    {
        this.classLoader = mock();

        this.resouceURL = new URL("http://host");
        this.prefixedResourceURL = new URL("http://host");
        when(this.classLoader.getResource(RESOURCE_NAME)).thenReturn(this.resouceURL);
        when(this.classLoader.getResource(RESOURCE_NAME_BACK)).thenReturn(this.resouceURL);
        when(this.classLoader.getResource(PREFIXED_RESOURCE_NAME)).thenReturn(this.prefixedResourceURL);
        when(this.classLoader.getResource(PREFIXED_RESOURCE_NAME_BACK)).thenReturn(this.prefixedResourceURL);

        this.inputStream = mock();
        this.prefixedInputStream = mock();
        when(this.classLoader.getResourceAsStream(RESOURCE_NAME)).thenReturn(this.inputStream);
        when(this.classLoader.getResourceAsStream(RESOURCE_NAME_BACK)).thenReturn(this.inputStream);
        when(this.classLoader.getResourceAsStream(PREFIXED_RESOURCE_NAME)).thenReturn(this.prefixedInputStream);
        when(this.classLoader.getResourceAsStream(PREFIXED_RESOURCE_NAME_BACK)).thenReturn(this.prefixedInputStream);
    }

    @Test
    void getResource()
    {
        assertSame(this.resouceURL, ClassLoaderUtils.getResource(this.classLoader, RESOURCE_NAME));
        assertSame(this.resouceURL, ClassLoaderUtils.getResource(this.classLoader, RESOURCE_NAME_BACK));

        assertThrows(IllegalArgumentException.class, () -> ClassLoaderUtils.getResource(this.classLoader, ".."));
        assertThrows(IllegalArgumentException.class, () -> ClassLoaderUtils.getResource(this.classLoader, "./.."));
        assertThrows(IllegalArgumentException.class,
            () -> ClassLoaderUtils.getResource(this.classLoader, "resource/../.."));

        assertSame(this.prefixedResourceURL, ClassLoaderUtils.getResource(this.classLoader, PREFIX, RESOURCE_NAME));
        assertSame(this.prefixedResourceURL, ClassLoaderUtils.getResource(this.classLoader, PREFIX, RESOURCE_NAME_BACK));

        assertThrows(IllegalArgumentException.class,
            () -> ClassLoaderUtils.getResource(this.classLoader, PREFIX, ".."));
        assertThrows(IllegalArgumentException.class,
            () -> ClassLoaderUtils.getResource(this.classLoader, PREFIX, "resource/../.."));
    }

    @Test
    void getResourceAsStream()
    {
        assertSame(this.inputStream, ClassLoaderUtils.getResourceAsStream(this.classLoader, RESOURCE_NAME));
        assertSame(this.inputStream, ClassLoaderUtils.getResourceAsStream(this.classLoader, RESOURCE_NAME_BACK));

        assertThrows(IllegalArgumentException.class,
            () -> ClassLoaderUtils.getResourceAsStream(this.classLoader, ".."));
        assertThrows(IllegalArgumentException.class,
            () -> ClassLoaderUtils.getResourceAsStream(this.classLoader, "./.."));
        assertThrows(IllegalArgumentException.class,
            () -> ClassLoaderUtils.getResourceAsStream(this.classLoader, "resource/../.."));

        assertSame(this.prefixedInputStream,
            ClassLoaderUtils.getResourceAsStream(this.classLoader, PREFIX, RESOURCE_NAME));
        assertSame(this.prefixedInputStream,
            ClassLoaderUtils.getResourceAsStream(this.classLoader, PREFIX, RESOURCE_NAME_BACK));

        assertThrows(IllegalArgumentException.class,
            () -> ClassLoaderUtils.getResourceAsStream(this.classLoader, PREFIX, ".."));
        assertThrows(IllegalArgumentException.class,
            () -> ClassLoaderUtils.getResourceAsStream(this.classLoader, PREFIX, "resource/../.."));
    }
}
