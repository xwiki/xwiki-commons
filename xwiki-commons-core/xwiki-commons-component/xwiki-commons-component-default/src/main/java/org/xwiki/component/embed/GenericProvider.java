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

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import org.xwiki.component.internal.RoleHint;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.ReflectionUtils;

/**
 * Default provider used when the Component Manager needs to inject a {@link Provider} field but no custom Provider has
 * been registered. The default behavior is simply to look up the Component role class when
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
     *            {@link javax.inject.Provider#get()}
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
        Object component;

        try {
            Class roleClass = this.roleHint.getRoleClass();

            if (Provider.class.isAssignableFrom(roleClass)) {
                // Then get the class the Provider is providing for
                Type providedType = ReflectionUtils.getLastTypeGenericArgument(this.roleHint.getRoleType());
                // Then lookup for a Provider registered with the default hint and for the Component Role it
                // provides
                Provider< ? > provider = this.componentManager.lookupProvider(providedType, this.roleHint.getHint());
                if (provider != null) {
                    component = provider;
                } else {
                    // Inject a default Provider
                    component =
                        new GenericProvider(this.componentManager, new RoleHint(providedType, this.roleHint.getHint()));
                }
            } else if (List.class.isAssignableFrom(roleClass)) {
                component =
                    this.componentManager.lookupList(ReflectionUtils.getTypeClass(ReflectionUtils
                        .getLastTypeGenericArgument(this.roleHint.getRoleType())));
            } else if (Map.class.isAssignableFrom(roleClass)) {
                component =
                    this.componentManager.lookupMap(ReflectionUtils.getTypeClass(ReflectionUtils
                        .getLastTypeGenericArgument(this.roleHint.getRoleType())));
            } else {
                component = this.componentManager.lookup(roleClass, this.roleHint.getHint());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to get [" + this.roleHint + "]", e);
        }

        return component;
    }
}
