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
package org.xwiki.test;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.descriptor.ComponentDescriptor;

/**
 * Tests which needs to have XWiki Components set up should extend this class which makes the Component Manager
 * available. Use this class for JUnit 4.x tests.
 * <p>
 * XWiki 2.2M1 also introduced {@link org.xwiki.test.AbstractMockingComponentTestCase} which provides automatic mocking
 * for injected component dependencies and which is thus better when writing pure unit tests, isolated from the rest.
 * </p><p>
 * Consider using this class only for integration tests.
 * </p>
 */
public abstract class AbstractComponentTestCase extends AbstractMockingTestCase
{
    private XWikiComponentInitializer initializer = new XWikiComponentInitializer();

    private ComponentAnnotationLoader componentLoader;

    /**
     * Tests that require fine-grained initializations can override this method and not call super.
     */
    @Before
    public void setUp() throws Exception
    {
        this.initializer.initializeConfigurationSource();

        // Put before execution context initialization because it could be needed for some executing context
        // initializer.
        registerComponents();

        this.initializer.initializeExecution();
    }

    /**
     * Clean up test states.
     */
    @After
    public void tearDown() throws Exception
    {
        this.initializer.shutdown();
    }

    /**
     * Register custom/mock components
     */
    protected void registerComponents() throws Exception
    {
        // Empty voluntarily. Extending classes can override to provide custom component registration.
    }

    @Override
    public MockingComponentManager getComponentManager() throws Exception
    {
        return this.initializer.getComponentManager();
    }

    /**
     * @return a modifiable mock configuration source
     */
    public MockConfigurationSource getConfigurationSource()
    {
        return this.initializer.getConfigurationSource();
    }

    /**
     * @since 3.2M3
     */
    public void registerComponent(Class< ? > componentClass) throws Exception
    {
        if (this.componentLoader == null) {
            this.componentLoader = new ComponentAnnotationLoader();
        }

        List<ComponentDescriptor> descriptors = this.componentLoader.getComponentsDescriptors(componentClass);

        for (ComponentDescriptor descriptor : descriptors) {
            getComponentManager().registerComponent(descriptor);
        }
    }
}
