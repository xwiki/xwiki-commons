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
import org.apache.velocity.tools.generic.NumberTool;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.test.annotation.AfterComponent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.velocity.VelocityConfiguration;
import org.xwiki.velocity.VelocityContextInitializer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultVelocityContextFactory}.
 *
 * @version $Id$
 */
@ComponentTest
public class DefaultVelocityContextFactoryTest
{
    @MockComponent
    private VelocityConfiguration configuration;

    @MockComponent
    private ComponentManager componentManager;

    @InjectMockComponents
    private DefaultVelocityContextFactory factory;

    @AfterComponent
    public void afterComponent()
    {
        Properties properties = new Properties();
        properties.put("numbertool", NumberTool.class.getName());
        when(this.configuration.getTools()).thenReturn(properties);
    }

    /**
     * Verify that we get different contexts when we call the createContext method but that they contain the same
     * references to the Velocity tools. Also tests that objects we put in one context are not shared with other
     * contexts. Also verifies that Velocity Context Initializers are called.
     */
    @Test
    void createDifferentContext() throws Exception
    {
        // We also verify that the VelocityContextInitializers are called.
        VelocityContextInitializer mockInitializer = mock(VelocityContextInitializer.class);
        when(this.componentManager.getInstanceList(VelocityContextInitializer.class))
            .thenReturn(Arrays.asList(mockInitializer));

        VelocityContext context1 = this.factory.createContext();
        context1.put("param", "value");
        VelocityContext context2 = this.factory.createContext();

        verify(mockInitializer, times(2)).initialize(any(VelocityContext.class));
        verify(this.componentManager, times(2)).getInstanceList(VelocityContextInitializer.class);

        assertNotSame(context1, context2);
        assertNotNull(context1.get("numbertool"));
        assertSame(context2.get("numbertool"), context1.get("numbertool"));
        assertNull(context2.get("param"));
    }
}
