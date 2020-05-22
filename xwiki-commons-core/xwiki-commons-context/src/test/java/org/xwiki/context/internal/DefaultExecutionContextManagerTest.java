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
package org.xwiki.context.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextInitializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link ExecutionContext}.
 *
 * @version $Id$
 * @since 1.8RC3
 */
@SuppressWarnings("unchecked")
class DefaultExecutionContextManagerTest
{
    Execution execution = new DefaultExecution();

    DefaultExecutionContextManager contextManager = new DefaultExecutionContextManager();

    @BeforeEach
    void beforeEach()
    {
        ReflectionUtils.setFieldValue(contextManager, "execution", execution);

        // Set up an Execution Context Initiliazer for the test
        final ExecutionContextInitializer initializer = new ExecutionContextInitializer()
        {
            @Override
            public void initialize(ExecutionContext context) throws ExecutionContextException
            {
                context.setProperty("key", Arrays.asList("value"));
            }
        };
        Provider<List<ExecutionContextInitializer>> provider = new Provider<List<ExecutionContextInitializer>>()
        {
            @Override
            public List<ExecutionContextInitializer> get()
            {
                return Arrays.asList(initializer);
            }
        };

        ReflectionUtils.setFieldValue(contextManager, "initializerProvider", provider);
    }

    /**
     * Verify we have different objects in the Execution Context after the clone.
     */
    @Test
    void cloneExecutionContext() throws Exception
    {
        ExecutionContext context = new ExecutionContext();
        this.execution.setContext(context);

        Map<Object, Object> xwikicontext = new HashMap<>();
        context.newProperty("property1").initial(xwikicontext).inherited().declare();
        context.newProperty("property2").initial(xwikicontext).inherited().makeFinal().cloneValue().declare();

        ExecutionContext clonedContext = contextManager.clone(context);

        assertSame(context, execution.getContext());
        assertEquals("value", ((List<String>) clonedContext.getProperty("key")).get(0));
        assertNotSame(context.getProperty("key"), clonedContext.getProperty("key"));
        assertSame(xwikicontext, clonedContext.getProperty("property1"));
        assertNotSame(xwikicontext, clonedContext.getProperty("property2"));
    }

    @Test
    void pushContext() throws ExecutionContextException
    {
        ReflectionUtils.setFieldValue(contextManager, "execution", execution);

        execution.pushContext(new ExecutionContext());

        execution.getContext().newProperty("inherited").inherited().initial("value").declare();

        contextManager.pushContext(new ExecutionContext(), true);

        assertNull(execution.getContext().getProperty("key"));

        assertNotNull(execution.getContext().getProperty("inherited"));

        contextManager.pushContext(new ExecutionContext(), false);

        assertEquals("value", ((List<String>) execution.getContext().getProperty("key")).get(0));

        assertNull(execution.getContext().getProperty("inherited"));
    }
}
