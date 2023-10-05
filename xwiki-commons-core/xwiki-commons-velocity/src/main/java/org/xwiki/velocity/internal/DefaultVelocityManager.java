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
package org.xwiki.velocity.internal;

import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.script.ScriptContext;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.logging.LoggerConfiguration;
import org.xwiki.script.ScriptContextManager;
import org.xwiki.velocity.ScriptVelocityContext;
import org.xwiki.velocity.VelocityConfiguration;
import org.xwiki.velocity.VelocityEngine;
import org.xwiki.velocity.VelocityManager;
import org.xwiki.velocity.VelocityTemplate;
import org.xwiki.velocity.XWikiVelocityException;
import org.xwiki.velocity.internal.directive.TryCatchDirective;

/**
 * Note: This class should be moved to the Velocity module. However this is not possible right now since we need to
 * populate the Velocity Context with XWiki objects that are located in the Core (such as the XWiki object for example)
 * and since the Core needs to call the Velocity module this would cause a circular dependency.
 *
 * @version $Id$
 * @since 15.9-rc-1
 */
@Component
@Singleton
public class DefaultVelocityManager implements VelocityManager, Initializable
{
    /**
     * Used to access the current {@link org.xwiki.context.ExecutionContext}.
     */
    @Inject
    protected Execution execution;

    /**
     * Used to get the current script context.
     */
    @Inject
    private ScriptContextManager scriptContextManager;

    @Inject
    private LoggerConfiguration loggerConfiguration;

    /**
     * Velocity configuration to get the list of configured Velocity properties.
     */
    @Inject
    private VelocityConfiguration velocityConfiguration;

    @Inject
    private ComponentManager componentManager;

    @Inject
    private Provider<InternalVelocityEngine> engineProvider;

    /**
     * Binding that should stay on Velocity side only.
     */
    private final Set<String> reservedBindings = new HashSet<>();

    /**
     * The actual Velocity runtime.
     */
    private RuntimeInstance runtimeInstance;

    private VelocityEngine mainVelocityEngine;

    @Override
    public void initialize() throws InitializationException
    {
        // Set reserved bindings

        // "context" is a reserved binding in JSR223 world
        this.reservedBindings.add("context");

        // Macros directive
        this.reservedBindings.add("macro");
        // Foreach directive
        this.reservedBindings.add("foreach");
        // Evaluate directive
        this.reservedBindings.add("evaluate");
        // TryCatch directive
        this.reservedBindings.add("exception");
        this.reservedBindings.add("try");
        // Default directive
        this.reservedBindings.add("define");
        // The name of the context variable used for the template-level scope
        this.reservedBindings.add("template");
    }

    private RuntimeInstance getRuntimeInstance() throws XWikiVelocityException
    {
        if (this.runtimeInstance == null) {
            this.runtimeInstance = initializeRuntimeInstance(this.componentManager, this.velocityConfiguration);
        }

        return this.runtimeInstance;
    }

    private synchronized RuntimeInstance initializeRuntimeInstance(ComponentManager componentManager,
        VelocityConfiguration velocityConfiguration) throws XWikiVelocityException
    {
        RuntimeInstance runtime = new RuntimeInstance();

        // Add the Component Manager to allow Velocity extensions to lookup components.
        runtime.setApplicationAttribute(ComponentManager.class.getName(), componentManager);

        // Set up properties
        initializeProperties(runtime, velocityConfiguration.getProperties());

        // Set up directives
        runtime.loadDirective(TryCatchDirective.class.getName());

        try {
            runtime.init();
        } catch (Exception e) {
            throw new XWikiVelocityException("Cannot start the Velocity engine", e);
        }

        return runtime;
    }

    /**
     * @param runtime the Velocity engine against which to initialize Velocity properties
     * @param configurationProperties the Velocity properties coming from XWiki's configuration
     */
    private void initializeProperties(RuntimeInstance runtime, Properties configurationProperties)
    {
        // Avoid "unable to find resource 'VM_global_library.vm' in any resource loader." if no
        // Velocimacro library is defined.
        runtime.setProperty(RuntimeConstants.VM_LIBRARY, "");

        // Configure Velocity by passing the properties defined in this component's configuration
        if (configurationProperties != null) {
            runtime.setProperties(configurationProperties);
        }
    }

    /**
     * @param runtimeInstance the actual Velocity instance to use as reference
     * @param name the name of the Velocity script
     * @param source the source of the Velocity script
     * @return the compiled script
     */
    public static VelocityTemplate compile(String name, Reader source, RuntimeInstance runtimeInstance)
    {
        // Create the template
        VelocityTemplate template = new VelocityTemplate(name, runtimeInstance);

        // Compile the template
        template.compile(source);

        return template;
    }

    @Override
    public VelocityTemplate compile(String name, Reader source) throws XWikiVelocityException
    {
        return compile(name, source, getRuntimeInstance());
    }

    @Override
    public VelocityContext getVelocityContext()
    {
        return getScriptVelocityContext();
    }

    protected ScriptVelocityContext getScriptVelocityContext()
    {
        ScriptVelocityContext velocityContext;

        // Make sure the velocity context support ScriptContext synchronization
        VelocityContext currentVelocityContext = getCurrentVelocityContext();
        if (currentVelocityContext instanceof ScriptVelocityContext) {
            velocityContext = (ScriptVelocityContext) currentVelocityContext;
        } else {
            velocityContext = new ScriptVelocityContext(currentVelocityContext,
                this.loggerConfiguration.isDeprecatedLogEnabled(), this.reservedBindings);
            ExecutionContext eContext = this.execution.getContext();
            if (eContext != null) {
                eContext.setProperty(VelocityExecutionContextInitializer.VELOCITY_CONTEXT_ID, velocityContext);
            }
        }

        // Synchronize with ScriptContext
        ScriptContext scriptContext = this.scriptContextManager.getScriptContext();
        velocityContext.setScriptContext(scriptContext);

        return velocityContext;
    }

    @Override
    public VelocityContext getCurrentVelocityContext()
    {
        ExecutionContext eContext = this.execution.getContext();

        return eContext != null
            ? (VelocityContext) eContext.getProperty(VelocityExecutionContextInitializer.VELOCITY_CONTEXT_ID) : null;
    }

    @Override
    public VelocityEngine getVelocityEngine() throws XWikiVelocityException
    {
        if (this.mainVelocityEngine == null) {
            this.mainVelocityEngine = createVelocityEngine();
        }

        return this.mainVelocityEngine;
    }

    protected synchronized VelocityEngine createVelocityEngine() throws XWikiVelocityException
    {
        InternalVelocityEngine velocityEngine = this.engineProvider.get();

        velocityEngine.initialize(getRuntimeInstance());

        return velocityEngine;
    }

    @Override
    public boolean evaluate(Writer out, String templateName, Reader source) throws XWikiVelocityException
    {
        // Get up to date Velocity context
        VelocityContext velocityContext = getVelocityContext();

        // Execute Velocity context
        return getVelocityEngine().evaluate(velocityContext, out, templateName, source);
    }
}
