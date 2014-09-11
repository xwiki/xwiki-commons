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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.groovy.GroovyConfiguration;
import org.xwiki.test.jmock.AbstractMockingComponentTestCase;
import org.xwiki.test.jmock.annotation.MockingRequirement;

/**
 * Test Groovy Execution with some Compilation Customizers. This test also tests the {@link GroovyScriptEngineFactory}
 * class.
 *
 * @version $Id$
 * @since 4.1M1
 */
@MockingRequirement(GroovyScriptEngineFactory.class)
public class GroovyExecutionTest extends AbstractMockingComponentTestCase<ScriptEngineFactory>
{
    @Before
    public void configure() throws Exception
    {
        getMockery().setImposteriser(ClassImposteriser.INSTANCE);
    }

    @Test
    public void execute() throws Exception
    {
        final GroovyConfiguration configuration = getComponentManager().getInstance(GroovyConfiguration.class);
        final CompilationCustomizer customizer = getMockery().mock(CompilationCustomizer.class);

        getMockery().checking(new Expectations()
        {{
                oneOf(configuration).getCompilationCustomizers();
                will(returnValue(Arrays.asList(customizer)));

                // Simulate a Compilation Customizer that throws an error. This would happend for example with a Secure
                // Customizer that would prevent executing some statements for example.
                oneOf(customizer).getPhase();
                will(returnValue(CompilePhase.CANONICALIZATION));
                oneOf(customizer).needSortedInput();
                will(returnValue(false));
                oneOf(customizer).call(with(any(SourceUnit.class)), with(any(GeneratorContext.class)),
                        with(any(ClassNode.class)));
                will(throwException(new SecurityException("test exception")));
            }});

        ScriptEngineManager manager = new ScriptEngineManager();
        manager.registerEngineName("groovy", getMockedComponent());

        ScriptEngine engine = manager.getEngineByName("groovy");

        try {
            engine.eval("def dummy");
        } catch (ScriptException expected) {
            Assert.assertTrue(expected.getMessage().contains("test exception"));
        }
    }
}
