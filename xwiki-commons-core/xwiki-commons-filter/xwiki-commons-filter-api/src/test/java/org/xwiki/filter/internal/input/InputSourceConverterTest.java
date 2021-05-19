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
package org.xwiki.filter.internal.input;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.filter.input.FileInputSource;
import org.xwiki.filter.input.InputSource;
import org.xwiki.filter.input.InputSourceReferenceParser;
import org.xwiki.filter.input.ReaderInputSource;
import org.xwiki.filter.input.StringInputSource;
import org.xwiki.filter.input.URLInputSource;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

/**
 * Validate {@link InputSourceConverter}.
 * 
 * @version $Id$
 */
@ComponentTest
@ComponentList(DefaultInputSourceReferenceParser.class)
class InputSourceConverterTest
{
    @InjectMockComponents
    private InputSourceConverter converter;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @MockComponent
    @Named("context")
    private Provider<ComponentManager> contextComponentManager;

    @BeforeEach
    void beforeEach()
    {
        when(this.contextComponentManager.get()).thenReturn(this.componentManager);
    }

    @Test
    void convertFromStringString()
    {
        InputSource source = this.converter.convert(InputSource.class, "string:content");

        assertThat(source, instanceOf(StringInputSource.class));
        assertEquals("content", ((StringInputSource) source).getSource());
    }

    @Test
    void convertFromFile()
    {
        File file = new File("myfile");

        InputSource source = this.converter.convert(InputSource.class, file);

        assertThat(source, instanceOf(FileInputSource.class));
        assertSame(file, ((FileInputSource) source).getFile());
    }

    @Test
    void convertFromInputStream()
    {
        File file = new File("myfile");

        InputSource source = this.converter.convert(InputSource.class, file);

        assertThat(source, instanceOf(FileInputSource.class));
        assertSame(file, ((FileInputSource) source).getFile());
    }

    @Test
    void convertFromReader()
    {
        Reader reader = new StringReader("content");

        InputSource source = this.converter.convert(InputSource.class, reader);

        assertThat(source, instanceOf(ReaderInputSource.class));
        assertSame(reader, ((ReaderInputSource) source).getReader());
    }

    @Test
    void convertFromURL() throws MalformedURLException
    {
        URL url = new URL("http://myurl");

        InputSource source = this.converter.convert(InputSource.class, url);

        assertThat(source, instanceOf(URLInputSource.class));
        assertSame(url, ((URLInputSource) source).getURL());
    }

    @Test
    void convertFromCustom() throws Exception
    {
        InputSource source = new StringInputSource("");
        org.xwiki.filter.input.InputSourceConverter<InputSourceConverterTest> testConverter =
            this.componentManager.registerMockComponent(TypeUtils
                .parameterize(org.xwiki.filter.input.InputSourceConverter.class, InputSourceConverterTest.class));
        when(testConverter.convert(this)).thenReturn(source);

        assertSame(source, this.converter.convert(InputSource.class, this));
    }

    @Test
    void convertFromFileString()
    {
        InputSource source = this.converter.convert(InputSource.class, "file:myfile");

        assertThat(source, instanceOf(FileInputSource.class));
        assertEquals(new File("myfile"), ((FileInputSource) source).getFile());
    }

    @Test
    void convertFromURLString()
    {
        InputSource source = this.converter.convert(InputSource.class, "url:http://myurl");

        assertThat(source, instanceOf(URLInputSource.class));
        assertEquals("http://myurl", ((URLInputSource) source).getURL().toExternalForm());
    }

    @Test
    void convertFromCustomString() throws Exception
    {
        InputSource source = new StringInputSource("");
        InputSourceReferenceParser testParser =
            this.componentManager.registerMockComponent(InputSourceReferenceParser.class, "test");
        when(testParser.parse("testvalue")).thenReturn(source);

        assertSame(source, this.converter.convert(InputSource.class, "test:testvalue"));
    }
}
