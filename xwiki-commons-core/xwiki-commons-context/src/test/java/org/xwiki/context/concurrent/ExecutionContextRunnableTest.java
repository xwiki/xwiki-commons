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
package org.xwiki.context.concurrent;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * Validate {@link ExecutionContextRunnable}.
 *
 * @version $Id$
 */
public class ExecutionContextRunnableTest
{
    @Test
    public void initializedExecutionContext() throws InterruptedException, ComponentLookupException,
        ExecutionContextException
    {
        ComponentManager componentMangerMock = Mockito.mock(ComponentManager.class);
        ExecutionContextManager executionContextManagerMock = Mockito.mock(ExecutionContextManager.class);
        Execution executionMock = Mockito.mock(Execution.class);

        Mockito.when(componentMangerMock.getInstance(ExecutionContextManager.class)).thenReturn(
            executionContextManagerMock);
        Mockito.when(componentMangerMock.getInstance(Execution.class)).thenReturn(executionMock);

        Runnable runnableMock = Mockito.mock(Runnable.class);

        Thread thread = new Thread(new ExecutionContextRunnable(runnableMock, componentMangerMock));

        thread.start();

        thread.join(10000);

        verify(executionContextManagerMock).initialize(any(ExecutionContext.class));
        verify(executionMock).removeContext();
        verify(runnableMock).run();
    }
}
