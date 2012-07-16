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
package org.xwiki.component.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;

/**
 * Provide the {@link ComponentManager} associated to the current context. Return the root {@link ComponentManager} if
 * no context oriented {@link ComponentManager} can be found.
 * 
 * @version $Id$
 * @since 4.1.4
 */
@Component
@Named("context")
@Singleton
public class ContextComponentManagerProvider implements Provider<ComponentManager>
{
    /**
     * The root {@link ComponentManager} used to lookup the context {@link ComponentManager} and as a fallback if none
     * is provided.
     */
    @Inject
    private ComponentManager rootComponentManager;

    @Override
    public ComponentManager get()
    {
        ComponentManager componentManagerToUse;

        // Look for the Context Component Manager so that Macros can be registered for a specific user, for a
        // specific wiki, etc. If it's not found use the Root Component Manager. This allows the Rendering module
        // to work outside of XWiki when there's no notion of Execution Context and Wiki Model for example.
        try {
            componentManagerToUse = this.rootComponentManager.getInstance(ComponentManager.class, "context");
        } catch (ComponentLookupException e) {
            // This means the Context CM doesn't exist, use the Root CM.
            componentManagerToUse = this.rootComponentManager;
        }

        return componentManagerToUse;
    }

}
