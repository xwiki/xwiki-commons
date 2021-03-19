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
import java.io.StringReader;
import java.io.Writer;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.StopCommand;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.velocity.VelocityConfiguration;
import org.xwiki.velocity.VelocityContextFactory;
import org.xwiki.velocity.VelocityEngine;
import org.xwiki.velocity.XWikiVelocityException;
import org.xwiki.velocity.internal.directive.TryCatchDirective;

/**
 * Default implementation of the Velocity service which initializes the Velocity system using configuration values
 * defined in the component's configuration. Note that the {@link #initialize} method has to be executed before any
 * other method can be called.
 *
 * @version $Id$
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultVelocityEngine implements VelocityEngine
{
    private static final String ECONTEXT_TEMPLATES = "velocity.templates";

    private static class SingletonResourceReader extends StringResourceLoader
    {
        private final Reader reader;

        SingletonResourceReader(Reader r)
        {
            this.reader = r;
        }

        @Override
        public Reader getResourceReader(String source, String encoding)
        {
            return this.reader;
        }
    }

    private class TemplateEntry
    {
        private Template template;

        private String namespace;

        private int counter;

        /**
         * @param namespace the namespace
         */
        TemplateEntry(String namespace)
        {
            this.namespace = namespace;
            this.template = new Template();
            this.template.setName(namespace);
            this.template.setRuntimeServices(runtimeInstance);

            this.counter = 1;

            if (globalEntry != null) {
                // Inject global macros
                this.template.getMacros().putAll(globalEntry.getTemplate().getMacros());
            }
        }

        Template getTemplate()
        {
            return this.template;
        }

        String getNamespace()
        {
            return this.namespace;
        }

        int getCounter()
        {
            return this.counter;
        }

        int incrementCounter()
        {
            ++this.counter;

            return this.counter;
        }

        int decrementCounter()
        {
            --this.counter;

            return this.counter;
        }
    }

    /**
     * Used to set it as a Velocity Application Attribute so that Velocity extensions done by XWiki can use it to lookup
     * other components.
     */
    @Inject
    private ComponentManager componentManager;

    /**
     * Velocity configuration to get the list of configured Velocity properties.
     */
    @Inject
    private VelocityConfiguration velocityConfiguration;

    /**
     * Used to create a new context whenever one isn't already provided to the
     * {@link #evaluate(Context, Writer, String, Reader)} method.
     */
    @Inject
    private VelocityContextFactory velocityContextFactory;

    @Inject
    private Execution execution;

    /**
     * The logger to use for logging.
     */
    @Inject
    private Logger logger;

    /**
     * The actual Velocity runtime.
     */
    private RuntimeInstance runtimeInstance;

    private TemplateEntry globalEntry;

    @Override
    public void initialize(Properties overridingProperties) throws XWikiVelocityException
    {
        RuntimeInstance runtime = new RuntimeInstance();

        // Add the Component Manager to allow Velocity extensions to lookup components.
        runtime.setApplicationAttribute(ComponentManager.class.getName(), this.componentManager);

        // Set up properties
        initializeProperties(runtime, this.velocityConfiguration.getProperties(), overridingProperties);

        // Set up directives
        runtime.loadDirective(TryCatchDirective.class.getName());

        try {
            runtime.init();
        } catch (Exception e) {
            throw new XWikiVelocityException("Cannot start the Velocity engine", e);
        }

        this.runtimeInstance = runtime;

        this.globalEntry = new TemplateEntry("");
    }

    /**
     * @param runtime the Velocity engine against which to initialize Velocity properties
     * @param configurationProperties the Velocity properties coming from XWiki's configuration
     * @param overridingProperties the Velocity properties that override the properties coming from XWiki's
     *            configuration
     */
    private void initializeProperties(RuntimeInstance runtime, Properties configurationProperties,
        Properties overridingProperties)
    {
        // Avoid "unable to find resource 'VM_global_library.vm' in any resource loader." if no
        // Velocimacro library is defined. This value is overriden below.
        runtime.setProperty(RuntimeConstants.VM_LIBRARY, "");

        // Configure Velocity by passing the properties defined in this component's configuration
        if (configurationProperties != null) {
            for (Enumeration<?> e = configurationProperties.propertyNames(); e.hasMoreElements();) {
                String key = e.nextElement().toString();
                // Only set a property if it's not overridden by one of the passed properties
                if (overridingProperties == null || !overridingProperties.containsKey(key)) {
                    String value = configurationProperties.getProperty(key);
                    runtime.setProperty(key, value);
                    this.logger.debug("Setting property [{}] = [{}]", key, value);
                }
            }
        }

        // Override the component's static properties with the ones passed in parameter
        if (overridingProperties != null) {
            for (Enumeration<?> e = overridingProperties.propertyNames(); e.hasMoreElements();) {
                String key = e.nextElement().toString();
                String value = overridingProperties.getProperty(key);
                runtime.setProperty(key, value);

                this.logger.debug("Overriding property [{}] = [{}]", key, value);
            }
        }
    }

    @Override
    public boolean evaluate(Context context, Writer out, String templateName, String source)
        throws XWikiVelocityException
    {
        return evaluate(context, out, templateName, new StringReader(source));
    }

    @Override
    public boolean evaluate(Context context, Writer out, String namespace, Reader source) throws XWikiVelocityException
    {
        // Ensure that initialization has been called
        if (this.runtimeInstance == null) {
            throw new XWikiVelocityException("This Velocity Engine has not yet been initialized. "
                + "You must call its initialize() method before you can use it.");
        }

        // Save the current resource so that it's not broken by the currently executed one
        Resource currentResource;
        if (context instanceof VelocityContext) {
            currentResource = ((VelocityContext) context).getCurrentResource();
        } else {
            currentResource = null;
        }

        try {
            TemplateEntry templateEntry;
            if (StringUtils.isNotEmpty(namespace)) {
                templateEntry = startedUsingMacroNamespaceInternal(namespace);
            } else {
                templateEntry = this.globalEntry;
            }

            // Set source
            templateEntry.getTemplate().setResourceLoader(new SingletonResourceReader(source));

            // Compile the template
            templateEntry.getTemplate().process();

            // Execute the velocity script
            templateEntry.getTemplate().merge(context != null ? context : this.velocityContextFactory.createContext(),
                out);

            return true;
        } catch (StopCommand s) {
            // Someone explicitly stopped the script with something like #stop. No reason to make a scene.
            return true;
        } catch (Exception e) {
            throw new XWikiVelocityException("Failed to evaluate content with namespace [" + namespace + "]", e);
        } finally {
            if (StringUtils.isNotEmpty(namespace)) {
                stoppedUsingMacroNamespace(namespace);
            }

            // Restore the current resource
            if (context instanceof VelocityContext) {
                ((VelocityContext) context).setCurrentResource(currentResource);
            }

            // Clean the introspection cache to avoid memory leak
            cleanIntrospectionCache(context);
        }
    }

    private void cleanIntrospectionCache(Context context)
    {
        try {
            Map introspectionCache = (Map) FieldUtils.readField(context, "introspectionCache", true);
            introspectionCache.clear();
        } catch (IllegalAccessException e) {
            this.logger.warn("Failed to clean the Velocity context introspection cache: ",
                ExceptionUtils.getRootCauseMessage(e));
        }
    }
    
    @Override
    @Deprecated
    public void clearMacroNamespace(String namespace)
    {
        // Does not really make much sense anymore since macros are now stored in the execution context
    }

    @Override
    public void startedUsingMacroNamespace(String namespace)
    {
        startedUsingMacroNamespaceInternal(namespace);
    }

    private Deque<TemplateEntry> getCurrentTemplates(boolean create)
    {
        ExecutionContext executionContext = this.execution.getContext();

        if (executionContext == null) {
            return null;
        }

        Deque<TemplateEntry> templates = (Deque<TemplateEntry>) executionContext.getProperty(ECONTEXT_TEMPLATES);

        if (templates == null && create) {
            templates = new LinkedList<>();
            if (!executionContext.hasProperty(ECONTEXT_TEMPLATES)) {
                executionContext.newProperty(ECONTEXT_TEMPLATES).inherited().declare();
            }
            executionContext.setProperty(ECONTEXT_TEMPLATES, templates);
        }

        return templates;
    }

    private TemplateEntry startedUsingMacroNamespaceInternal(String namespace)
    {
        Deque<TemplateEntry> templates = getCurrentTemplates(true);

        TemplateEntry templateEntry;
        if (templates != null) {
            // If this is already the current template namespace increment the counter, otherwise push a new
            // TemplateEntry
            if (!templates.isEmpty() && templates.peek().getNamespace().equals(namespace)) {
                templateEntry = templates.peek();
                templateEntry.incrementCounter();
            } else {
                templateEntry = new TemplateEntry(namespace);
                templates.push(templateEntry);
            }
        } else {
            // If no execution context can be found create a new template entry
            templateEntry = new TemplateEntry(namespace);
        }

        return templateEntry;
    }

    @Override
    public void stoppedUsingMacroNamespace(String namespace)
    {
        Deque<TemplateEntry> templates = getCurrentTemplates(true);

        if (templates != null) {
            if (templates.isEmpty()) {
                this.logger.warn("Impossible to pop namespace [{}] because there is no namespace in the stack",
                    namespace);
            } else {
                popTemplateEntry(templates, namespace);
            }
        }
    }

    private void popTemplateEntry(Deque<TemplateEntry> templates, String namespace)
    {
        TemplateEntry templateEntry = templates.peek();

        if (templateEntry.getNamespace().equals(namespace)) {
            if (templateEntry.getCounter() > 1) {
                templateEntry.decrementCounter();
            } else {
                templates.pop();
            }
        } else {
            this.logger.warn("Impossible to pop namespace [{}] because current namespace is [{}]", namespace,
                templateEntry.getNamespace());
        }
    }
}
