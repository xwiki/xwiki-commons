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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextInitializer;
import org.xwiki.context.ExecutionContextManager;

/**
 * Default implementation of {@link ExecutionContextManager}.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultExecutionContextManager implements ExecutionContextManager
{
    /**
     * Used to set the {@link ExecutionContext}.
     */
    @Inject
    private Execution execution;

    /**
     * Used to initialize the passed {@link ExecutionContext}. Note that we use a Provider so that the loading of all
     * the Initializers is done lazily when they are required. This allows Extensions to contribute Initializers.
     * Incidentally it also prevents creating Component Manager cycles since an Initializer could depend on something
     * required to initialize this class.
     */
    @Inject
    private Provider<List<ExecutionContextInitializer>> initializerProvider;

    @Override
    public ExecutionContext clone(ExecutionContext context) throws ExecutionContextException
    {
        ExecutionContext clonedContext = new ExecutionContext();

        // Ideally we would like to do a deep cloning here. However it's just too hard since we don't control
        // objects put in the Execution Context and they can be of any type, including Maps which are cloneable
        // but only do shallow clones.
        // Thus instead we recreate the Execution Context from scratch and reinitialize it by calling all the
        // Execution Context Initializers on it.
        try {
            this.execution.pushContext(clonedContext);
        } catch (RuntimeException e) {
            // If inheritance fails, we will get an unchecked exception here. So we'll wrap it in an
            // ExecutionContextException.
            throw new ExecutionContextException("Failed to push cloned execution context.", e);
        }
        try {
            runInitializers(clonedContext);
        } finally {
            // #initialize set the context but we just want to clone it so we need to restore it
            this.execution.popContext();
        }

        return clonedContext;
    }

    @Override
    public void initialize(ExecutionContext context) throws ExecutionContextException
    {
        // Make sure we set Execution Context in the Execution component before we call the initialization
        // so that we don't get any NPE if some initializer code asks to get the Execution Context. This
        // happens for example with the Velocity Execution Context initializer which in turns calls the Velocity
        // Context initializers and some of them look inside the Execution Context.
        //
        // Also, inherited values will be copied from the current context, if there is a current context.
        try {
            this.execution.setContext(context);
        } catch (RuntimeException e) {
            // If inheritance fails, we will get an unchecked exception here. So we'll wrap it in an
            // ExecutionContextException.
            throw new ExecutionContextException("Failed to set the execution context.", e);
        }

        runInitializers(context);
    }

    @Override
    public void pushContext(ExecutionContext context, boolean inherit) throws ExecutionContextException
    {
        this.execution.pushContext(context, inherit);

        if (!inherit) {
            runInitializers(context);
        }
    }

    @Override
    public void popContext()
    {
        this.execution.popContext();
    }

    /**
     * Run the initializers.
     *
     * @param context the execution context to initialize
     * @throws ExecutionContextException in case one {@link ExecutionContextInitializer} fails to execute
     */
    private void runInitializers(ExecutionContext context) throws ExecutionContextException
    {
        for (ExecutionContextInitializer initializer : this.initializerProvider.get()) {
            initializer.initialize(context);
        }
    }
}
