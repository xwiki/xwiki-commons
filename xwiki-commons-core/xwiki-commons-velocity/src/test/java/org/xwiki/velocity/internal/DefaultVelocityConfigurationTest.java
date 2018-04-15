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

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.tools.generic.ListTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.velocity.introspection.DeprecatedCheckUberspector;
import org.xwiki.velocity.introspection.MethodArgumentsUberspector;
import org.xwiki.velocity.introspection.SecureUberspector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultVelocityConfiguration}.
 *
 * @version $Id$
 * @since 2.4RC1
 */
@ComponentTest
public class DefaultVelocityConfigurationTest
{
    @InjectMockComponents
    private DefaultVelocityConfiguration configuration;

    @BeforeEach
    public void configure(ComponentManager componentManager) throws Exception
    {
        ConfigurationSource source = componentManager.getInstance(ConfigurationSource.class);
        when(source.getProperty("velocity.tools", Properties.class)).thenReturn(new Properties());
        when(source.getProperty("velocity.properties", Properties.class)).thenReturn(new Properties());
    }

    @Test
    public void testDefaultToolsPresent() throws Exception
    {
        // Verify for example that the List tool is present.
        assertEquals(ListTool.class.getName(), this.configuration.getTools().get("listtool"));
    }

    @Test
    public void testDefaultPropertiesPresent() throws Exception
    {
        // Verify that the secure uberspector is set by default
        assertEquals(StringUtils.join(new String[] { SecureUberspector.class.getName(),
            DeprecatedCheckUberspector.class.getName(), MethodArgumentsUberspector.class.getName() }, ','),
            this.configuration.getProperties().getProperty("runtime.introspector.uberspect"));

        // Verify that null values are allowed by default
        assertEquals(Boolean.TRUE.toString(),
            this.configuration.getProperties().getProperty("directive.set.null.allowed"));

        // Verify that Macros are isolated by default
        assertEquals(Boolean.TRUE.toString(),
            this.configuration.getProperties().getProperty("velocimacro.permissions.allow.inline.local.scope"));
    }
}
