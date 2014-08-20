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

import java.util.Iterator;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.ToolInfo;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.config.PropertiesFactoryConfiguration;
import org.apache.velocity.tools.config.ToolConfiguration;
import org.apache.velocity.tools.config.ToolboxConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.velocity.VelocityConfiguration;
import org.xwiki.velocity.VelocityContextFactory;
import org.xwiki.velocity.VelocityContextInitializer;
import org.xwiki.velocity.XWikiVelocityException;

/**
 * Default implementation for {@link VelocityContextFactory}.
 * 
 * @version $Id$
 */
@Component
@Singleton
public class DefaultVelocityContextFactory implements VelocityContextFactory, Initializable
{
    /**
     * The component manager we used to find all components implementing the
     * {@link org.xwiki.velocity.VelocityContextInitializer} role.
     */
    @Inject
    private ComponentManager componentManager;

    /**
     * Velocity configuration to get the list of configured Velocity tools.
     */
    @Inject
    private VelocityConfiguration velocityConfiguration;

    /**
     * Velocity-Tools Manager that handles the registered velocity tools and performs per-scope instance caching (when
     * needed) across all the velocity context created from it.
     */
    private ToolManager velocityToolManager;

    @Override
    public void initialize() throws InitializationException
    {
        velocityToolManager = new ToolManager();

        PropertiesFactoryConfiguration toolsConfiguration = new PropertiesFactoryConfiguration()
        {
            @Override
            public void validate()
            {
                // Do nothing.
            }
            
            protected void readTools(ExtendedProperties tools, ToolboxConfiguration toolbox)
            {
                for (Iterator i = tools.getKeys(); i.hasNext();) {
                    String key = (String) i.next();
                    // if it contains a period, it can't be a context key;
                    // it must be a tool property. ignore it for now.
                    if (key.indexOf('.') >= 0) {
                        continue;
                    }

                    String classname = tools.getString(key);
                    ToolConfiguration tool = new ToolConfiguration() {
                        public ToolInfo createInfo()
                        {
                            ToolInfo info = new ToolInfo(getKey(), getToolClass());

                            info.restrictTo(getRestrictTo());
                            if (getSkipSetters() != null) {
                                info.setSkipSetters(getSkipSetters());
                            }
                            // it's ok to use this here, because we know it's the
                            // first time properties have been added to this ToolInfo
                            info.addProperties(getPropertyMap());
                            return info;
                        }
                    };
                    tool.setClassname(classname);
                    tool.setKey(key);
                    toolbox.addTool(tool);

                    // get tool properties prefixed by 'property'
                    ExtendedProperties toolProps = tools.subset(key);
                    readProperties(toolProps, tool);

                    // ok, get tool properties that aren't prefixed by 'property'
                    for (Iterator j = toolProps.getKeys(); j.hasNext();) {
                        String name = (String) j.next();
                        if (!name.equals(tool.getKey())) {
                            tool.setProperty(name, toolProps.getString(name));
                        }
                    }

                    // get special props explicitly
                    String restrictTo = toolProps.getString("restrictTo");
                    tool.setRestrictTo(restrictTo);
                }
            }
        };

        // Load the default tool properties and the ones defined in xwiki.properties into a configuration.
        Properties toolsProperties = this.velocityConfiguration.getTools();
        toolsConfiguration.read(ExtendedProperties.convertProperties(toolsProperties));

        velocityToolManager.configure(toolsConfiguration);
    }

    @Override
    public VelocityContext createContext() throws XWikiVelocityException
    {
        VelocityContext context = new VelocityContext(velocityToolManager.createContext());

        // Call all components implementing the VelocityContextInitializer's role.
        try {
            for (Object interceptor : this.componentManager.getInstanceList(VelocityContextInitializer.class)) {
                ((VelocityContextInitializer) interceptor).initialize(context);
            }
        } catch (ComponentLookupException e) {
            throw new XWikiVelocityException("Failed to locate some Velocity Context initializers", e);
        }

        return context;
    }
}
