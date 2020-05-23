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
package org.xwiki.groovy.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.junit.jupiter.api.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.groovy.GroovyCompilationCustomizer;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultGroovyConfiguration}.
 *
 * @version $Id$
 * @since 4.1M1
 */
@ComponentTest
class DefaultGroovyConfigurationTest
{
    @InjectMockComponents
    private DefaultGroovyConfiguration configuration;

    @MockComponent
    private ConfigurationSource source;

    @MockComponent
    @Named("mycustomizer")
    private GroovyCompilationCustomizer customizer;

    @Test
    void getCustomizersWhenNoCustomizersDeclared()
    {
        when(this.source.getProperty("groovy.compilationCustomizers", Collections.emptyList())).thenReturn(
            Collections.emptyList());

        List<CompilationCustomizer> customizers = this.configuration.getCompilationCustomizers();
        assertEquals(0, customizers.size());
    }

    @Test
    void getCustomizersWhenCustomizersDeclared()
    {
        when(this.source.getProperty("groovy.compilationCustomizers", Collections.emptyList())).thenReturn(
            Arrays.asList("mycustomizer"));
        when(this.customizer.createCustomizer()).thenReturn(new CompilationCustomizer(CompilePhase.PARSING)
        {
            @Override
            public void call(SourceUnit source, GeneratorContext context, ClassNode classNode)
                throws CompilationFailedException
            {
                // Stub for testing, do nothing.
            }
        });

        List<CompilationCustomizer> customizers = this.configuration.getCompilationCustomizers();
        assertEquals(1, customizers.size());
        assertTrue(customizers.get(0) instanceof CompilationCustomizer);
    }
}
