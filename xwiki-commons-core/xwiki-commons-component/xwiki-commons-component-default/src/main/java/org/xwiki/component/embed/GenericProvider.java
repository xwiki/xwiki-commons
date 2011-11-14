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
package org.xwiki.component.embed;

import javax.inject.Provider;

import org.xwiki.component.internal.RoleHint;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;

/**
 * Default provider used when the Component Manager needs to inject a {@link Provider} field but no custom Provider
 * has been registered. The default behavior is simply to look up the Component role class when
 * {@link javax.inject.Provider#get()} is called. This is useful for example when you wish to do "lazy injection".
 * 
 * @version $Id$
 * @since 3.3M2
 */
public class GenericProvider implements Provider<Object>
{
    /**
     * @see GenericProvider#GenericProvider(ComponentManager, RoleHint)
     */
    private ComponentManager componentManager;

    /**
     * @see GenericProvider#GenericProvider(ComponentManager, RoleHint)
     */
    private RoleHint roleHint;

    /**
     * @param componentManager the Component Manager instance that we'll use to look up the Component Role during
     *        {@link javax.inject.Provider#get()}
     * @param roleHint the Component Role and Hint that uniquely identify the Component we wish to provide for
     */
    public GenericProvider(ComponentManager componentManager, RoleHint roleHint)
    {
        this.componentManager = componentManager;
        this.roleHint = roleHint;
    }

    @Override
    public Object get()
    {
        try {
            return this.componentManager.lookup(this.roleHint.getRole(), this.roleHint.getHint());
        } catch (ComponentLookupException e) {
            throw new RuntimeException("Failed to get [" + this.roleHint + "]", e);
        }
    }
}
