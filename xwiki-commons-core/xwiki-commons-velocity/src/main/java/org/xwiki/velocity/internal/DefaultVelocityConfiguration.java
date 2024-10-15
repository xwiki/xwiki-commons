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

import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.generic.LogTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.util.introspection.DeprecatedCheckUberspector;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.logging.LoggerConfiguration;
import org.xwiki.velocity.VelocityConfiguration;
import org.xwiki.velocity.internal.util.RestrictParseLocationEventHandler;
import org.xwiki.velocity.introspection.MethodArgumentsUberspector;
import org.xwiki.velocity.introspection.SecureUberspector;
import org.xwiki.velocity.tools.CollectionTool;
import org.xwiki.velocity.tools.ComparisonDateTool;
import org.xwiki.velocity.tools.EscapeTool;
import org.xwiki.velocity.tools.JSONTool;
import org.xwiki.velocity.tools.ObjectTool;
import org.xwiki.velocity.tools.RegexTool;
import org.xwiki.velocity.tools.StringTool;
import org.xwiki.velocity.tools.URLTool;
import org.xwiki.velocity.tools.nio.NIOTool;

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
     * Used to find out if deprecated log is enabled by default.
     */
    @Inject
    protected LoggerConfiguration loggerConfiguration;

    /**
     * Defines from where to read the rendering configuration data.
     */
    @Inject
    protected ConfigurationSource configuration;

    @Inject
    protected List<DefaultToolsInitializer> toolsInitializers;

    /**
     * Default Tools.
     */
    protected Properties defaultTools = new Properties();

    /**
     * Default properties.
     */
    protected Properties defaultProperties = new Properties();

    @Override
    public void initialize() throws InitializationException
    {
        // Default Velocity tools.
        this.defaultTools.setProperty("numbertool", NumberTool.class.getName());
        this.defaultTools.setProperty("datetool", ComparisonDateTool.class.getName());
        this.defaultTools.setProperty("mathtool", MathTool.class.getName());
        this.defaultTools.setProperty(EscapeTool.DEFAULT_KEY, EscapeTool.class.getName());
        this.defaultTools.setProperty("regextool", RegexTool.class.getName());
        this.defaultTools.setProperty("collectiontool", CollectionTool.class.getName());
        this.defaultTools.setProperty("stringtool", StringTool.class.getName());
        this.defaultTools.setProperty("jsontool", JSONTool.class.getName());
        this.defaultTools.setProperty("urltool", URLTool.class.getName());
        this.defaultTools.setProperty("exceptiontool", ExceptionUtils.class.getName());
        this.defaultTools.setProperty("niotool", NIOTool.class.getName());
        this.defaultTools.setProperty("logtool", LogTool.class.getName());
        this.defaultTools.setProperty("objecttool", ObjectTool.class.getName());

        // Extension point to inject other default tools
        this.toolsInitializers.forEach(l -> l.initialize(this.defaultTools));

        // Default Velocity properties
        this.defaultProperties.setProperty(RuntimeConstants.VM_MAX_DEPTH, "100");
        this.defaultProperties.setProperty(RuntimeConstants.RESOURCE_MANAGER_LOGWHENFOUND, Boolean.FALSE.toString());
        this.defaultProperties.setProperty(RuntimeConstants.VM_PERM_INLINE_LOCAL, Boolean.TRUE.toString());
        // Allow to override global macros and to use properly skin macros.
        this.defaultProperties.setProperty(RuntimeConstants.VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL,
            Boolean.TRUE.toString());

        // [Retro compatibility]
        // * Make empty string #if evaluate to true
        this.defaultProperties.setProperty(RuntimeConstants.CHECK_EMPTY_OBJECTS, Boolean.FALSE.toString());
        // * Use Velocity 1.x Space Gobbling
        this.defaultProperties.setProperty(RuntimeConstants.SPACE_GOBBLING, "bc");
        // * Allow "-" in variables names
        this.defaultProperties.setProperty(RuntimeConstants.PARSER_HYPHEN_ALLOWED, Boolean.TRUE.toString());
        // * Keep original variable name when passing null parameter
        // * Use global context as default value for missing macro parameters
        this.defaultProperties.setProperty(RuntimeConstants.VM_ENABLE_BC_MODE, Boolean.TRUE.toString());

        // Enable the extra scope variables $template and $macro, similar to $foreach
        this.defaultProperties.setProperty(RuntimeConstants.CONTEXT_SCOPE_CONTROL + ".template",
            Boolean.TRUE.toString());
        this.defaultProperties.setProperty(RuntimeConstants.CONTEXT_SCOPE_CONTROL + ".macro", Boolean.TRUE.toString());

        // Prevents users from calling #parse on files outside the /templates/ directory
        this.defaultProperties.setProperty(RuntimeConstants.EVENTHANDLER_INCLUDE,
            RestrictParseLocationEventHandler.class.getName());

        // The uberspectors enabled by default
        initializeDefaultUberspectors();
    }

    private void initializeDefaultUberspectors()
    {
        StringBuilder unberspectors = new StringBuilder();

        // Block access to dangerous APIs
        unberspectors.append(SecureUberspector.class.getName());

        // Warning logs when using deprecated APIs
        if (this.loggerConfiguration.isDeprecatedLogEnabled()) {
            unberspectors.append(',');
            unberspectors.append(DeprecatedCheckUberspector.class.getName());
        }

        // Auto conversion of method parameters
        unberspectors.append(',');
        unberspectors.append(MethodArgumentsUberspector.class.getName());

        this.defaultProperties.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, unberspectors.toString());
    }

    @Override
    public Properties getProperties()
    {
        // Merge default properties and properties defined in the configuration
        Properties props = new Properties();
        props.putAll(this.defaultProperties);

        Properties configuredProperties = this.configuration.getProperty(PREFIX + "properties", Properties.class);
        if (configuredProperties != null) {
            props.putAll(configuredProperties);
        }

        return props;
    }

    @Override
    public Properties getTools()
    {
        // Merge default tools and tools defined in the configuration
        Properties props = new Properties();
        props.putAll(this.defaultTools);

        Properties configuredTools = this.configuration.getProperty(PREFIX + "tools", Properties.class);
        if (configuredTools != null) {
            props.putAll(configuredTools);
        }

        return props;
    }
}
