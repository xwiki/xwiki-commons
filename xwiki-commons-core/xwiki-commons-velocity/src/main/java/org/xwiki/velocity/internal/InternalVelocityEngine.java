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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.resource.Resource;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.velocity.VelocityContextFactory;
import org.xwiki.velocity.VelocityEngine;
import org.xwiki.velocity.VelocityTemplate;
import org.xwiki.velocity.XWikiVelocityException;

/**
 * Default implementation of the Velocity service which initializes the Velocity system using configuration values
 * defined in the component's configuration. Note that the {@link #initialize} method has to be executed before any
 * other method can be called.
 *
 * @version $Id$
 * @since 15.9RC1
 */
@Component(roles = InternalVelocityEngine.class)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class InternalVelocityEngine implements VelocityEngine
{
    private static final String ECONTEXT_TEMPLATES = "velocity.templates";

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

    /**
     * @param runtimeInstance the actual Velocity runtime
     */
    public void initialize(RuntimeInstance runtimeInstance)
    {
        this.runtimeInstance = runtimeInstance;

        this.globalEntry = new TemplateEntry("");
    }

    @Override
    public void addGlobalMacros(Map<String, Object> macros)
    {
        this.globalEntry.getTemplate().getMacros().putAll(macros);
    }

    @Override
    public boolean evaluate(Context context, Writer out, String namespace, String source) throws XWikiVelocityException
    {
        return evaluate(context, out, namespace, new StringReader(source));
    }

    @Override
    public boolean evaluate(Context context, Writer out, String namespace, Reader source) throws XWikiVelocityException
    {
        // Create the template
        VelocityTemplate template = DefaultVelocityManager.compile(namespace, source, this.runtimeInstance);

        evaluate(context, out, namespace, template);

        return true;
    }

    private TemplateEntry pushNamespace(String namespace)
    {
        TemplateEntry templateEntry;
        if (StringUtils.isNotEmpty(namespace)) {
            templateEntry = startedUsingMacroNamespaceInternal(namespace);
        } else {
            templateEntry = this.globalEntry;
        }

        return templateEntry;
    }

    private void popNamespace(String namespace)
    {
        if (StringUtils.isNotEmpty(namespace)) {
            stoppedUsingMacroNamespace(namespace);
        }
    }

    @Override
    public void evaluate(Context context, Writer out, String namespace, VelocityTemplate template)
        throws XWikiVelocityException
    {
        // Save some contextual metadata that needs to be restored
        Resource currentResource = null;
        List<Template> currentMacroLibraries = null;
        if (context instanceof VelocityContext) {
            currentResource = ((VelocityContext) context).getCurrentResource();
            currentMacroLibraries = ((VelocityContext) context).getMacroLibraries();
        }

        try {
            // Find current library template
            TemplateEntry templateEntry = pushNamespace(namespace);

            // Inject the current template macro in the library
            templateEntry.getTemplate().getMacros().putAll(template.getMacros());

            // Make sure to have a context
            Context mergeContext = context != null ? context : this.velocityContextFactory.createContext();

            // Set current library template
            if (context instanceof VelocityContext) {
                ((VelocityContext) context).setMacroLibraries(List.of(templateEntry.getTemplate()));
            }

            // Execute the velocity script
            template.getTemplate().merge(mergeContext, out);
        } catch (Exception e) {
            throw new XWikiVelocityException(String.format("Failed to evaluate content with namespace [%s]", namespace),
                e);
        } finally {
            // Restore current library template
            popNamespace(namespace);

            // Restore the current resource
            if (context instanceof VelocityContext) {
                ((VelocityContext) context).setCurrentResource(currentResource);
                ((VelocityContext) context).setMacroLibraries(currentMacroLibraries);
            }

            // Clean the introspection cache to avoid memory leak
            cleanIntrospectionCache(context);
        }
    }

    private void cleanIntrospectionCache(Context context)
    {
        if (context != null) {
            try {
                Map introspectionCache = (Map) FieldUtils.readField(context, "introspectionCache", true);
                introspectionCache.clear();
            } catch (IllegalAccessException e) {
                this.logger.warn("Failed to clean the Velocity context introspection cache. Root error: [{}]",
                    ExceptionUtils.getRootCauseMessage(e));
            }
        }
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
