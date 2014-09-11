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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.velocity.introspection.ChainingUberspector;
import org.xwiki.velocity.introspection.DeprecatedCheckUberspector;
import org.xwiki.velocity.introspection.MethodArgumentsUberspector;
import org.xwiki.velocity.introspection.SecureUberspector;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultVelocityConfiguration}.
 *
 * @version $Id$
 * @since 2.4RC1
 */
public class DefaultVelocityConfigurationTest
{
    @Rule
    public MockitoComponentMockingRule<DefaultVelocityConfiguration> mocker =
        new MockitoComponentMockingRule(DefaultVelocityConfiguration.class);

    @Before
    public void configure() throws Exception
    {
        ConfigurationSource source = this.mocker.getInstance(ConfigurationSource.class);
        when(source.getProperty("velocity.tools", Properties.class)).thenReturn(new Properties());
        when(source.getProperty("velocity.properties", Properties.class)).thenReturn(new Properties());
    }

    @Test
    public void testDefaultToolsPresent() throws Exception
    {
        // Verify for example that the List tool is present.
        assertEquals(ListTool.class.getName(), this.mocker.getComponentUnderTest().getTools().get("listtool"));
    }

    @Test
    public void testDefaultPropertiesPresent() throws Exception
    {
        // Verify that the secure uberspector is set by default
        assertEquals(ChainingUberspector.class.getName(),
            this.mocker.getComponentUnderTest().getProperties().getProperty("runtime.introspector.uberspect"));
        assertEquals(StringUtils.join(new String[] { SecureUberspector.class.getName(),
            DeprecatedCheckUberspector.class.getName(), MethodArgumentsUberspector.class.getName() }, ','),
            this.mocker.getComponentUnderTest().getProperties().getProperty(
                "runtime.introspector.uberspect.chainClasses"));

        // Verify that null values are allowed by default
        assertEquals(Boolean.TRUE.toString(),
            this.mocker.getComponentUnderTest().getProperties().getProperty("directive.set.null.allowed"));

        // Verify that Macros are isolated by default
        assertEquals(Boolean.TRUE.toString(), this.mocker.getComponentUnderTest().getProperties().getProperty(
            "velocimacro.permissions.allow.inline.local.scope"));
    }
}
