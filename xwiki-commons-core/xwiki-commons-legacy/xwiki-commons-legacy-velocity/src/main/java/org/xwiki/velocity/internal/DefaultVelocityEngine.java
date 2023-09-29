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
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.velocity.VelocityConfiguration;
import org.xwiki.velocity.VelocityEngine;
import org.xwiki.velocity.VelocityTemplate;
import org.xwiki.velocity.XWikiVelocityException;
import org.xwiki.velocity.internal.directive.TryCatchDirective;

/**
 * Default implementation of the Velocity service which initializes the Velocity system using configuration values
 * defined in the component's configuration. Note that the {@link #initialize} method has to be executed before any
 * other method can be called.
 *
 * @version $Id$
 * @since use {@link InternalVelocityEngine} instead
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
@Deprecated(since = "15.9-rc-1")
public class DefaultVelocityEngine implements VelocityEngine
{
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

    @Inject
    private InternalVelocityEngine xwikiVelocityEngine;

    /**
     * The logger to use for logging.
     */
    @Inject
    private Logger logger;

    @Override
    @Deprecated
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

        this.xwikiVelocityEngine.initialize(runtime);
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
    public void addGlobalMacros(Map<String, Object> macros)
    {
        this.xwikiVelocityEngine.addGlobalMacros(macros);
    }

    @Override
    public boolean evaluate(Context context, Writer out, String namespace, String source) throws XWikiVelocityException
    {
        return this.xwikiVelocityEngine.evaluate(context, out, namespace, source);
    }

    @Override
    public boolean evaluate(Context context, Writer out, String namespace, Reader source) throws XWikiVelocityException
    {
        return this.xwikiVelocityEngine.evaluate(context, out, namespace, source);
    }

    @Override
    public void evaluate(Context context, Writer out, String namespace, VelocityTemplate template)
        throws XWikiVelocityException
    {
        this.xwikiVelocityEngine.evaluate(context, out, namespace, template);
    }

    @Override
    public void startedUsingMacroNamespace(String namespace)
    {
        this.xwikiVelocityEngine.startedUsingMacroNamespace(namespace);
    }

    @Override
    public void stoppedUsingMacroNamespace(String namespace)
    {
        this.xwikiVelocityEngine.stoppedUsingMacroNamespace(namespace);
    }
}
