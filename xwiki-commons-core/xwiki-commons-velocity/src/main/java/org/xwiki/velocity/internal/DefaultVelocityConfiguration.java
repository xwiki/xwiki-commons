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

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.tools.generic.ComparisonDateTool;
import org.apache.velocity.tools.generic.ListTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.tools.generic.SortTool;
import org.apache.velocity.util.introspection.SecureUberspector;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.velocity.VelocityConfiguration;
import org.xwiki.velocity.introspection.ChainingUberspector;
import org.xwiki.velocity.introspection.DeprecatedCheckUberspector;
import org.xwiki.velocity.tools.EscapeTool;
import org.xwiki.velocity.tools.RegexTool;

/**
 * All configuration options for the Velocity subsystem.
 * 
 * @version $Id$
 * @since 2.0M1
 */
@Component
@Singleton
public class DefaultVelocityConfiguration implements Initializable, VelocityConfiguration
{
    /**
     * Prefix for configuration keys for the Velocity module.
     */
    private static final String PREFIX = "velocity.";

    /**
     * Defines from where to read the rendering configuration data.
     */
    @Inject
    private ConfigurationSource configuration;

    /**
     * Default Tools.
     */
    private Properties defaultTools = new Properties();

    /**
     * Default properties.
     */
    private Properties defaultProperties = new Properties();

    @Override
    public void initialize() throws InitializationException
    {
        // Default Velocity tools.
        this.defaultTools.setProperty("listtool", ListTool.class.getName());
        this.defaultTools.setProperty("numbertool", NumberTool.class.getName());
        this.defaultTools.setProperty("datetool", ComparisonDateTool.class.getName());
        this.defaultTools.setProperty("mathtool", MathTool.class.getName());
        this.defaultTools.setProperty("sorttool", SortTool.class.getName());
        this.defaultTools.setProperty("escapetool", EscapeTool.class.getName());
        this.defaultTools.setProperty("regextool", RegexTool.class.getName());
        this.defaultTools.setProperty("stringtool", StringUtils.class.getName());

        // Default Velocity properties
        this.defaultProperties.setProperty("directive.set.null.allowed", Boolean.TRUE.toString());
        this.defaultProperties.setProperty("velocimacro.messages.on", Boolean.FALSE.toString());
        this.defaultProperties.setProperty("velocimacro.max.depth", "100");
        this.defaultProperties.setProperty("resource.manager.logwhenfound", Boolean.FALSE.toString());
        this.defaultProperties.setProperty("velocimacro.permissions.allow.inline.local.scope", Boolean.TRUE.toString());
        this.defaultProperties.setProperty("eventhandler.include.class", "org.xwiki.velocity.internal.util.XWikiIncludeEventHandler");
        // Prevents users from writing dangerous Velocity code like using Class.forName or Java threading APIs.
        this.defaultProperties.setProperty("runtime.introspector.uberspect", ChainingUberspector.class.getName());
        this.defaultProperties.setProperty("runtime.introspector.uberspect.chainClasses",
            SecureUberspector.class.getName() + "," + DeprecatedCheckUberspector.class.getName());
        // Enable the extra scope variables $template and $macro, similar to $foreach
        this.defaultProperties.setProperty("template.provide.scope.control", Boolean.TRUE.toString());
        this.defaultProperties.setProperty("macro.provide.scope.control", Boolean.TRUE.toString());
    }

    @Override
    public Properties getProperties()
    {
        // Merge default properties and properties defined in the configuration
        Properties props = new Properties();
        props.putAll(this.defaultProperties);
        props.putAll(this.configuration.getProperty(PREFIX + "properties", Properties.class));
        return props;
    }

    @Override
    public Properties getTools()
    {
        // Merge default tools and tools defined in the configuration
        Properties props = new Properties();
        props.putAll(this.defaultTools);
        props.putAll(this.configuration.getProperty(PREFIX + "tools", Properties.class));
        return props;
    }
}
