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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TimedInterruptGroovyCompilationCustomizer}.
 *
 * @version $Id$
 * @since 4.1M1
 */
@ComponentTest
@AllComponents
class TimedInterruptGroovyCompilationCustomizerTest
{
    @MockComponent
    private ConfigurationSource source;

    @InjectComponentManager
    private ComponentManager componentManager;

    @Test
    // Ensure that the test will fail after 10 seconds
    @Timeout(value = 10)
    void executeWithTimedInterruptCustomizer() throws Exception
    {
        when(source.getProperty("groovy.compilationCustomizers", Collections.emptyList())).thenReturn(
            Arrays.asList("timedInterrupt"));
        when(source.getProperty("groovy.customizer.timedInterrupt.timeout", 60L)).thenReturn(1L);

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngineFactory groovyScriptEngineFactory =
            this.componentManager.getInstance(ScriptEngineFactory.class, "groovy");
        manager.registerEngineName("groovy", groovyScriptEngineFactory);

        ScriptEngine engine = manager.getEngineByName("groovy");

        // Simulate an infinite loop to verify that we timeout after 1 second
        try {
            engine.eval("while (true) {}");
            fail("Should have thrown an exception here");
        } catch (ScriptException e) {
            assertTrue(e.getMessage().contains("Execution timed out after 1 seconds."));
        }
    }
}
