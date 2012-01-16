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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
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
     * The name of the key associated to the old XWiki context in the {@link ExecutionContext}.
     */
    private static final String XWIKICONTEXT_KEY = "xwikicontext";

    /**
     * The name of the key associated to the Velocity context in the {@link ExecutionContext}.
     */
    private static final String VELOCITY_KEY = "velocityContext";

    /**
     * Used to set the {@link ExecutionContext}.
     */
    @Inject
    private Execution execution;

    /**
     * Used to initialize the passed {@link ExecutionContext}.
     */
    @Inject
    private List<ExecutionContextInitializer> initializers = new ArrayList<ExecutionContextInitializer>();

    /**
     * Default constructor.
     */
    public DefaultExecutionContextManager()
    {
    }

    /**
     * Generally used for unit tests.
     * 
     * @param execution an {@link Execution} to use when initializing {@link ExecutionContext}.
     * @since 3.2RC1
     */
    public DefaultExecutionContextManager(Execution execution)
    {
        this.execution = execution;
    }

    @Override
    public ExecutionContext clone(ExecutionContext context) throws ExecutionContextException
    {
        ExecutionContext currentContext = this.execution.getContext();

        ExecutionContext clonedContext = new ExecutionContext();

        // Manually add the XWiki Context so that old code continues to work.
        // Make sure we set the XWiki context before calling initializer since some of them count on it.
        // FIXME: This has nothing to do in commons which does not know anything about XWikiContext. Find a cleaner way
        // like giving some properties to keeps as parameters of #clone.
        clonedContext.setProperty(XWIKICONTEXT_KEY, context.getProperty(XWIKICONTEXT_KEY));

        // Ideally we would like to do a deep cloning here. However it's just too hard since we don't control
        // objects put in the Execution Context and they can be of any type, including Maps which are cloneable
        // but only do shallow clones.
        // Thus instead we recreate the Execution Context from scratch and reinitialize it by calling all the
        // Execution Context Initializers on it.
        try {
            initialize(clonedContext);
        } finally {
            // #initialize set the context but we just want to clone it so we need to restore it
            this.execution.setContext(currentContext);
        }

        // Manually clone the Velocity Context too since currently the XWikiVelocityContextInitializer is not yet
        // implemented.
        // Note that we're using reflection since we don't want to add a dependency on Velocity module since that
        // would cause a cyclic dependency.
        // TODO: Fix this when XWikiVelocityContextInitializer is implemented
        // Note that Velocity doesn't provide a method for cloning a Velocity Context
        // (see https://issues.apache.org/jira/browse/VELOCITY-712). Thus we're not cloning the Velocity Context
        // which can raise problems if the included page modifies the Velocity Context...
        Object velocityContext = context.getProperty(VELOCITY_KEY);
        if (velocityContext != null) {
            try {
                clonedContext.setProperty(VELOCITY_KEY,
                    velocityContext.getClass().getMethod("clone").invoke(velocityContext));
            } catch (Exception e) {
                throw new ExecutionContextException(
                    "Failed to clone Velocity Context for the new Execution Context", e);
            }
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
        this.execution.setContext(context);

        for (ExecutionContextInitializer initializer : this.initializers) {
            initializer.initialize(context);
        }
    }

    /**
     * @param initializer the initializer to add to the list
     */
    public void addExecutionContextInitializer(ExecutionContextInitializer initializer)
    {
        this.initializers.add(initializer);
    }
}
