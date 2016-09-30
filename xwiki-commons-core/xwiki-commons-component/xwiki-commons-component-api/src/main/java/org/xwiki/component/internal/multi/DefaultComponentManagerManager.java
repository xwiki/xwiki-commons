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
package org.xwiki.component.internal.multi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.namespace.NamespaceUtils;

/**
 * Default implementation of {@link ComponentManagerManager}.
 *
 * @version $Id$
 * @since 3.3M2
 */
@Component
@Singleton
public class DefaultComponentManagerManager implements ComponentManagerManager
{
    /**
     * The Factory to use to create Component Managers whenever a component is registered for a key that doesn't already
     * exist.
     */
    @Inject
    private ComponentManagerFactory defaultComponentManagerFactory;

    /**
     * The root component manager.
     */
    @Inject
    private ComponentManager rootComponentManager;

    /**
     * Holds Component Managers based on identifiers.
     */
    private Map<String, ComponentManager> componentManagers = new ConcurrentHashMap<>();

    @Override
    public ComponentManager getComponentManager(String namespace, boolean create)
    {
        ComponentManager componentManager;
        if (namespace == null) {
            componentManager = this.rootComponentManager;
        } else {
            componentManager = this.componentManagers.get(namespace);
            if (componentManager == null && create) {
                componentManager = createComponentManager(namespace);
                this.componentManagers.put(namespace, componentManager);
            }
        }

        return componentManager;
    }

    /**
     * Create a new {@link ComponentManager} for the provided id.
     *
     * @param namespace the identifier of the component manager
     * @return a new {@link ComponentManager} instance
     */
    private ComponentManager createComponentManager(String namespace)
    {
        String prefix = NamespaceUtils.getPrefix(namespace);

        ComponentManagerFactory componentManagerFactory;
        try {
            componentManagerFactory = this.rootComponentManager.getInstance(ComponentManagerFactory.class, prefix);
        } catch (ComponentLookupException e) {
            componentManagerFactory = this.defaultComponentManagerFactory;
        }

        return componentManagerFactory.createComponentManager(namespace, this.rootComponentManager);
    }
}
