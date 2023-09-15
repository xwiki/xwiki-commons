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
package org.xwiki.script.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import javax.inject.Named;
import javax.inject.Provider;
import javax.script.ScriptContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.script.ScriptContextInitializer;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

/**
 * Unit tests for {@link DefaultScriptContextManager}.
 * 
 * @version $Id$
 */
@ComponentTest
class DefaultScriptContextManagerTest
{
    @InjectMockComponents
    private DefaultScriptContextManager scriptContextManager;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @MockComponent
    private Execution execution;

    @MockComponent
    @Named("context")
    private Provider<ComponentManager> contextComponentManagerProvider;

    @Mock
    private ExecutionContext executionContext;

    @Mock
    private ScriptContext scriptContext;

    @Mock
    private ComponentManager componentManager;

    @BeforeEach
    public void before()
    {
        when(this.contextComponentManagerProvider.get()).thenReturn(this.componentManager);
    }

    @Test
    void getScriptContextWithoutExecutionContext()
    {
        assertNull(this.scriptContextManager.getScriptContext());
    }

    @Test
    void getScriptContextWhenMissing()
    {
        when(this.execution.getContext()).thenReturn(this.executionContext);

        assertNull(this.scriptContextManager.getScriptContext());
    }

    @Test
    void getScriptContextWithGetInstanceListException() throws Exception
    {
        when(this.execution.getContext()).thenReturn(this.executionContext);
        when(this.executionContext.getProperty(ScriptExecutionContextInitializer.SCRIPT_CONTEXT_ID))
            .thenReturn(this.scriptContext);
        when(this.componentManager.getInstanceList(ScriptContextInitializer.class))
            .thenThrow(new ComponentLookupException("Some error"));

        assertSame(this.scriptContext, this.scriptContextManager.getScriptContext());

        assertEquals("Failed to get the list of script context initializers. "
            + "Root cause is [ComponentLookupException: Some error].", logCapture.getMessage(0));
    }

    @Test
    void getScriptContextWithEmptyList() throws Exception
    {
        when(this.execution.getContext()).thenReturn(this.executionContext);
        when(this.executionContext.getProperty(ScriptExecutionContextInitializer.SCRIPT_CONTEXT_ID))
            .thenReturn(this.scriptContext);
        when(this.componentManager.getInstanceList(ScriptContextInitializer.class))
            .thenReturn(Collections.emptyList());

        assertSame(this.scriptContext, this.scriptContextManager.getScriptContext());

        assertEquals(0, logCapture.size());
    }

    @Test
    void getScriptContext() throws Exception
    {
        when(this.execution.getContext()).thenReturn(this.executionContext);
        when(this.executionContext.getProperty(ScriptExecutionContextInitializer.SCRIPT_CONTEXT_ID))
            .thenReturn(this.scriptContext);

        ScriptContextInitializer firstScriptContextInitializer = mock(ScriptContextInitializer.class, "first");
        ScriptContextInitializer secondScriptContextInitializer = mock(ScriptContextInitializer.class, "second");
        when(this.componentManager.getInstanceList(ScriptContextInitializer.class))
            .thenReturn(Arrays.asList(firstScriptContextInitializer, secondScriptContextInitializer));
        
        // An exception in one script context initializer should not prevent the other script context initializers from
        // being executed.
        doThrow(new RuntimeException("Runtime error")).when(firstScriptContextInitializer).initialize(this.scriptContext);

        assertSame(this.scriptContext, this.scriptContextManager.getScriptContext());

        assertEquals(1, logCapture.size());
        assertEquals("Failed to initialize the script context with [first]. "
            + "Root cause is [RuntimeException: Runtime error].", logCapture.getMessage(0));
        verify(secondScriptContextInitializer).initialize(this.scriptContext);
    }
}
