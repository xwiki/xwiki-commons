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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.jmock.Expectations;
import org.junit.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.groovy.GroovyCompilationCustomizer;
import org.xwiki.groovy.GroovyConfiguration;
import org.xwiki.test.jmock.AbstractMockingComponentTestCase;
import org.xwiki.test.jmock.annotation.MockingRequirement;

import junit.framework.Assert;

/**
 * Unit tests for {@link DefaultGroovyConfiguration}.
 *
 * @version $Id$
 * @since 4.1M1
 */
@MockingRequirement(DefaultGroovyConfiguration.class)
public class DefaultGroovyConfigurationTest extends AbstractMockingComponentTestCase<GroovyConfiguration>
{
    @Test
    public void getCustomizersWhenNoCustomizersDeclared() throws Exception
    {
        final ConfigurationSource source = getComponentManager().getInstance(ConfigurationSource.class);
        getMockery().checking(new Expectations() {{
            oneOf(source).getProperty("groovy.compilationCustomizers", Collections.emptyList());
            will(returnValue(Collections.emptyList()));
        }});

        List<CompilationCustomizer> customizers = getMockedComponent().getCompilationCustomizers();
        Assert.assertEquals(0, customizers.size());
    }

    @Test
    public void getCustomizersWhenCustomizersDeclared() throws Exception
    {
        final ConfigurationSource source = getComponentManager().getInstance(ConfigurationSource.class);
        final ComponentManager componentManager = getComponentManager().getInstance(ComponentManager.class);
        final GroovyCompilationCustomizer customizer = getMockery().mock(GroovyCompilationCustomizer.class);

        getMockery().checking(new Expectations() {{
            oneOf(source).getProperty("groovy.compilationCustomizers", Collections.emptyList());
                will(returnValue(Arrays.asList("mycustomizer")));
            oneOf(componentManager).getInstance(GroovyCompilationCustomizer.class, "mycustomizer");
                will(returnValue(customizer));
            oneOf(customizer).createCustomizer();
                will(returnValue(new CompilationCustomizer(CompilePhase.PARSING)
                {
                    @Override public void call(SourceUnit source, GeneratorContext context, ClassNode classNode)
                        throws CompilationFailedException
                    {
                        // Stub for testing, do nothing.
                    }
                }));
        }});

        List<CompilationCustomizer> customizers = getMockedComponent().getCompilationCustomizers();
        Assert.assertEquals(1, customizers.size());
        Assert.assertTrue(customizers.get(0) instanceof CompilationCustomizer);
    }
}
