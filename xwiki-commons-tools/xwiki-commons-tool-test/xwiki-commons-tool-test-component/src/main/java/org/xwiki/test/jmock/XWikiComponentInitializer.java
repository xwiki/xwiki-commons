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
package org.xwiki.test.jmock;

import org.xwiki.component.internal.StackingComponentEventManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.test.internal.MockConfigurationSource;

/**
 * Initialize a component manager used in unit tests.
 *
 * @version $Id$
 * @deprecated use {@link org.xwiki.test.junit5.mockito.ComponentTest} instead
 */
@Deprecated(since = "4.3.1")
public class XWikiComponentInitializer
{
    private MockingComponentManager componentManager;

    private MockConfigurationSource configurationSource;

    public void initializeConfigurationSource() throws Exception
    {
        // Register the mock configuration source for different roles so that tests always use the mock
        this.configurationSource = new MockConfigurationSource();

        getComponentManager().registerComponent(MockConfigurationSource.getDescriptor(null), this.configurationSource);
        getComponentManager().registerComponent(MockConfigurationSource.getDescriptor("xwikiproperties"),
            this.configurationSource);
        getComponentManager().registerComponent(MockConfigurationSource.getDescriptor("xwikicfg"),
            this.configurationSource);
        getComponentManager().registerComponent(MockConfigurationSource.getDescriptor("wiki"),
            this.configurationSource);
        getComponentManager().registerComponent(MockConfigurationSource.getDescriptor("space"),
            this.configurationSource);
        getComponentManager().registerComponent(MockConfigurationSource.getDescriptor("user"),
            this.configurationSource);
        getComponentManager().registerComponent(MockConfigurationSource.getDescriptor("all"), this.configurationSource);
    }

    public void initializeExecution() throws Exception
    {
        // Initialize the Execution Context
        ExecutionContextManager ecm = getComponentManager().getInstance(ExecutionContextManager.class);

        ExecutionContext ec = new ExecutionContext();

        ecm.initialize(ec);
    }

    public void shutdown() throws Exception
    {
        Execution execution = getComponentManager().getInstance(Execution.class);
        execution.removeContext();

        // Clean possible resources some components might hold
        this.componentManager.dispose();

        // Make sure we mark the component manager for garbage collection as otherwise each JUnit test will
        // have an instance of the Component Manager (will all the components it's holding), leading to
        // out of memory errors when there are lots of tests...
        this.componentManager = null;
    }

    /**
     * @return a configured Component Manager
     */
    public MockingComponentManager getComponentManager() throws Exception
    {
        if (this.componentManager == null) {
            MockingComponentManager ecm = new MockingComponentManager();
            ecm.initialize(this.getClass().getClassLoader());
            ecm.setComponentEventManager(new StackingComponentEventManager());
            this.componentManager = ecm;
        }

        return this.componentManager;
    }

    /**
     * @return a modifiable mock configuration source
     */
    public MockConfigurationSource getConfigurationSource()
    {
        return this.configurationSource;
    }
}
