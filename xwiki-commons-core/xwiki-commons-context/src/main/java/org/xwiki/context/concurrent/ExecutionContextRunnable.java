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

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextManager;

/**
 * {@link Runnable} wrapper which initialize and clean the execution context.
 *
 * @version $Id$
 * @since 5.1RC1
 */
public class ExecutionContextRunnable implements Runnable
{
    /**
     * Used to access the components needed to initialize and dispose the {@link ExecutionContext}.
     */
    private ComponentManager componentManager;

    /**
     * The runnable to wrap.
     */
    private Runnable runnable;

    /**
     * @param runnable used to access the components needed to initialize and dispose the {@link ExecutionContext}.
     * @param componentManager the runnable to wrap.
     */
    public ExecutionContextRunnable(Runnable runnable, ComponentManager componentManager)
    {
        this.runnable = runnable;
        this.componentManager = componentManager;
    }

    @Override
    public void run()
    {
        // Create a clean Execution Context
        ExecutionContext context = new ExecutionContext();

        try {
            this.componentManager.<ExecutionContextManager>getInstance(ExecutionContextManager.class)
                .initialize(context);
        } catch (Exception e) {
            throw new RuntimeException(
                String.format("Failed to initialize Runnable [%s] execution context", this.runnable), e);
        }

        try {
            this.runnable.run();
        } finally {
            try {
                this.componentManager.<Execution>getInstance(Execution.class).removeContext();
            } catch (ComponentLookupException e) {
                throw new RuntimeException(String.format(
                    "Failed to cleanup Execution Context after Runnable [%s] execution", this.runnable),  e);
            }
        }
    }
}
