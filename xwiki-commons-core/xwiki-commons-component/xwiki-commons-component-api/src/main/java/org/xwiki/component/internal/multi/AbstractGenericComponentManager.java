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

import javax.inject.Inject;

import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;

/**
 * Generic implementation that creates Component Manager instances based on the generic notion of a key. This is used
 * for example by the Wiki Component Manager or the User Component Manager which respectively have a key returning the
 * current wiki and the current user in the Execution Context.
 * 
 * @version $Id$
 * @since 3.3M2
 */
public abstract class AbstractGenericComponentManager extends DelegateComponentManager
{
    /**
     * Store and provide {@link ComponentManager} instances.
     */
    @Inject
    private ComponentManagerManager componentManagerManager;

    /**
     * @see #getInternalParent()
     */
    private ComponentManager internalParent;

    /**
     * @return the key (any String) representing the current Component Manager. For example in the case of a User
     *         Component Manager the key can be the current user in the execution context so that we can register/lookup
     *         components only for that user
     */
    protected abstract String getKey();

    @Override
    public ComponentManager getComponentManager()
    {
        ComponentManager componentManager = this.componentManagerManager.getComponentManager(getKey(), false);
        if (componentManager == null) {
            // There's no specific Component Manager for the Current Context Component Manager.
            // Redirect to the Parent Component Manager if it exists and if not, then use the Null pattern
            // to return a Null Component Manager.
            componentManager = getInternalParent();
            if (componentManager == null) {
                throw new RuntimeException("Key-based Proxy Component Managers should always have a Parent Component "
                    + "Manager defined so that it can be used if there's no Component Manager for the given key");
            }
        }

        return componentManager;
    }

    @Override
    public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor, T componentInstance)
        throws ComponentRepositoryException
    {
        synchronized (this) {
            // Make sure the ComponentManager associated to the current key exists
            this.componentManagerManager.getComponentManager(getKey(), true);

            if (componentInstance == null) {
                super.registerComponent(componentDescriptor);
            } else {
                super.registerComponent(componentDescriptor, componentInstance);
            }
        }
    }

    @Override
    public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor) throws ComponentRepositoryException
    {
        registerComponent(componentDescriptor, null);
    }

    @Override
    public <T> void unregisterComponent(Class<T> role, String roleHint)
    {
        super.unregisterComponent(role, roleHint);

        // Note: Ideally if the Component Manager for the current key is empty we could remove it from the list
        // of managed Component Managers. However there's currently no way to ask a Component Manager for its
        // full list of managed components or whether it's empty or not.
    }

    /**
     * @return the Parent Component Manager to default to when there's no Component Manager matching the key returned by
     *         {@link #getKey()}.
     */
    protected ComponentManager getInternalParent()
    {
        return this.internalParent;
    }

    /**
     * @param parentComponentManager see {@link #getInternalParent()}
     */
    protected void setInternalParent(ComponentManager parentComponentManager)
    {
        this.internalParent = parentComponentManager;
    }
}
