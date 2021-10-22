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
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.junit.jupiter.api.Test;
import org.xwiki.groovy.GroovyConfiguration;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test Groovy Execution with some Compilation Customizers. This test also tests the {@link GroovyScriptEngineFactory}
 * class.
 *
 * @version $Id$
 * @since 4.1M1
 */
@ComponentTest
class GroovyExecutionTest
{
    @InjectMockComponents
    private GroovyScriptEngineFactory factory;

    @MockComponent
    private GroovyConfiguration configuration;

    @Test
    void executeWhenCompilationCustomizerThrowsException()
    {
        CompilationCustomizer customizer = mock(CompilationCustomizer.class);
        when(this.configuration.getCompilationCustomizers()).thenReturn(Arrays.asList(customizer));
        // Simulate a Compilation Customizer that throws an error. This would happen for example with a Secure
        // Customizer that would prevent executing some statements for example.
        when(customizer.getPhase()).thenReturn(CompilePhase.CANONICALIZATION);
        when(customizer.needSortedInput()).thenReturn(false);
        doThrow(new SecurityException("test exception")).when(customizer).call(any(SourceUnit.class),
            any(GeneratorContext.class), any(ClassNode.class));

        ScriptEngineManager manager = new ScriptEngineManager();
        manager.registerEngineName("groovy", this.factory);

        ScriptEngine engine = manager.getEngineByName("groovy");

        Throwable exception = assertThrows(ScriptException.class, () -> engine.eval("def dummy"));
        assertTrue(exception.getMessage().contains("test exception"));
    }
}
