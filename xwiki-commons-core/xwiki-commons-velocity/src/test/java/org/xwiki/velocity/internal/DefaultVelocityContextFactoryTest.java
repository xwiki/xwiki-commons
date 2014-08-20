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

import java.util.Arrays;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.generic.ListTool;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.test.annotation.AfterComponent;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.velocity.VelocityConfiguration;
import org.xwiki.velocity.VelocityContextFactory;
import org.xwiki.velocity.VelocityContextInitializer;
import org.junit.Assert;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultVelocityContextFactory}.
 *
 * @version $Id$
 */
public class DefaultVelocityContextFactoryTest
{
    @Rule
    public MockitoComponentMockingRule<DefaultVelocityContextFactory> mocker =
        new MockitoComponentMockingRule(DefaultVelocityContextFactory.class);

    private VelocityContextFactory factory;

    @Before
    public void configure() throws Exception
    {
        VelocityConfiguration configuration = this.mocker.getInstance(VelocityConfiguration.class);
        Properties properties = new Properties();
        properties.put("toolbox", "application");
        properties.put("application.listtool", ListTool.class.getName());
        when(configuration.getTools()).thenReturn(properties);

        this.factory = this.mocker.getInstance(VelocityContextFactory.class);
    }

    @AfterComponent
    public void overrideComponents() throws Exception
    {
        this.mocker.registerMockComponent(ComponentManager.class);
    }

    /**
     * Verify that we get different contexts when we call the createContext method but that they contain the same
     * references to the Velocity tools. Also tests that objects we put in one context are not shared with other
     * contexts. Also verifies that Velocity Context Initializers are called.
     */
    @Test
    public void createDifferentContext() throws Exception
    {
        // We also verify that the VelocityContextInitializers are called.
        VelocityContextInitializer mockInitializer = mock(VelocityContextInitializer.class);
        ComponentManager mockComponentManager = this.mocker.getInstance(ComponentManager.class);
        when(mockComponentManager.getInstanceList(VelocityContextInitializer.class)).thenReturn(
            Arrays.<Object>asList(mockInitializer));

        VelocityContext context1 = this.factory.createContext();
        context1.put("param", "value");
        VelocityContext context2 = this.factory.createContext();

        verify(mockInitializer, times(2)).initialize(any(VelocityContext.class));
        verify(mockComponentManager, times(2)).getInstanceList(VelocityContextInitializer.class);

        Assert.assertNotSame(context1, context2);
        Assert.assertNotNull(context1.get("listtool"));
        Assert.assertSame(context2.get("listtool"), context1.get("listtool"));
        Assert.assertNull(context2.get("param"));
    }
}
