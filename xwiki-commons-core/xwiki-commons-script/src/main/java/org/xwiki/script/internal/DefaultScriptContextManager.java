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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.script.ScriptContext;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.script.ScriptContextInitializer;
import org.xwiki.script.ScriptContextManager;

/**
 * Default implementation of {@link ScriptContextManager}.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultScriptContextManager implements ScriptContextManager
{
    /**
     * Used to get and insert script context in current execution context.
     */
    @Inject
    private Execution execution;

    /**
     * The {@link ScriptContextInitializer} list used to initialize {@link ScriptContext}.
     */
    @Inject
    private List<ScriptContextInitializer> scriptContextInitializerList;

    @Override
    public ScriptContext getScriptContext()
    {
        ScriptContext context = getCurrentScriptContext();

        if (context != null) {
            // We re-initialize the Script Context with all Script Context Initializers. We do this in order to ensure
            // that the Script Context always contain correct values even if user scripts or XWiki code have modified
            // them.
            // For example the current document in the Script Context could have changed and thus needs to be set back.
            // Also note that we don't clone the context since we want that in the same request several script
            // executions can share bindings.
            for (ScriptContextInitializer scriptContextInitializer : this.scriptContextInitializerList) {
                scriptContextInitializer.initialize(context);
            }
        }

        return context;
    }

    @Override
    public ScriptContext getCurrentScriptContext()
    {
        ExecutionContext executionContext = this.execution.getContext();

        if (executionContext != null) {
            // The Script Context is set in ScriptExecutionContextInitializer, when the XWiki Execution Context is
            // initialized so we are guaranteed it is defined when this method is called.
            return (ScriptContext) executionContext.getProperty(ScriptExecutionContextInitializer.SCRIPT_CONTEXT_ID);
        }

        return null;
    }
}
