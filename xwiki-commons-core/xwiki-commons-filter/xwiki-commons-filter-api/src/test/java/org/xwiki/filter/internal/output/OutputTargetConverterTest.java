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
package org.xwiki.filter.internal.output;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;

import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.filter.output.FileOutputTarget;
import org.xwiki.filter.output.OutputTarget;
import org.xwiki.filter.output.OutputTargetReferenceParser;
import org.xwiki.filter.output.StringWriterOutputTarget;
import org.xwiki.filter.output.WriterOutputTarget;
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
 * Validate {@link OutputTargetConverter}.
 * 
 * @version $Id$
 */
@ComponentTest
@ComponentList(DefaultOutputTargetReferenceParser.class)
class OutputTargetConverterTest
{
    @InjectMockComponents
    private OutputTargetConverter converter;

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
    void convertFromFile()
    {
        File file = new File("myfile");

        OutputTarget target = this.converter.convert(OutputTarget.class, file);

        assertThat(target, instanceOf(FileOutputTarget.class));
        assertSame(file, ((FileOutputTarget) target).getFile());
    }

    @Test
    void convertFromInputStream()
    {
        File file = new File("myfile");

        OutputTarget target = this.converter.convert(OutputTarget.class, file);

        assertThat(target, instanceOf(FileOutputTarget.class));
        assertSame(file, ((FileOutputTarget) target).getFile());
    }

    @Test
    void convertFromWriter()
    {
        Writer writer = new StringWriter();

        OutputTarget target = this.converter.convert(OutputTarget.class, writer);

        assertThat(target, instanceOf(WriterOutputTarget.class));
        assertSame(writer, ((WriterOutputTarget) target).getWriter());
    }

    @Test
    void convertFromCustom() throws Exception
    {
        OutputTarget target = new StringWriterOutputTarget();
        org.xwiki.filter.output.OutputTargetConverter<OutputTargetConverterTest> testConverter =
            this.componentManager.registerMockComponent(TypeUtils
                .parameterize(org.xwiki.filter.output.OutputTargetConverter.class, OutputTargetConverterTest.class));
        when(testConverter.convert(this)).thenReturn(target);

        assertSame(target, this.converter.convert(OutputTarget.class, this));
    }

    @Test
    void convertFromFileString()
    {
        OutputTarget target = this.converter.convert(OutputTarget.class, "file:myfile");

        assertThat(target, instanceOf(FileOutputTarget.class));
        assertEquals(new File("myfile"), ((FileOutputTarget) target).getFile());
    }

    @Test
    void convertFromCustomString() throws Exception
    {
        OutputTarget target = new StringWriterOutputTarget();
        OutputTargetReferenceParser testParser =
            this.componentManager.registerMockComponent(OutputTargetReferenceParser.class, "test");
        when(testParser.parse("testvalue")).thenReturn(target);

        assertSame(target, this.converter.convert(OutputTarget.class, "test:testvalue"));
    }
}
