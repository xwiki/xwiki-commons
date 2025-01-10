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

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import jakarta.inject.Provider;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.component.annotation.Role;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.ReflectionUtils;

/**
 * Default provider used when the Component Manager needs to inject a {@link Provider} field but no custom Provider has
 * been registered. The default behavior is simply to look up the Component role class when
 * {@link javax.inject.Provider#get()} is called. This is useful for example when you wish to do "lazy injection".
 *
 * @param <T> the role type
 * @version $Id$
 * @since 17.0.0RC1
 */
public abstract class AbstractGenericProvider<T>
{
    /**
     * @see AbstractGenericProvider#GenericProvider(ComponentManager, RoleHint)
     */
    protected ComponentManager componentManager;

    /**
     * @see AbstractGenericProvider#GenericProvider(ComponentManager, RoleHint)
     */
    private RoleHint<T> roleHint;

    /**
     * @param componentManager the Component Manager instance that we'll use to look up the Component Role during
     *            {@link javax.inject.Provider#get()}
     * @param roleHint the Component Role and Hint that uniquely identify the Component we wish to provide for
     */
    AbstractGenericProvider(ComponentManager componentManager, RoleHint<T> roleHint)
    {
        this.componentManager = componentManager;
        this.roleHint = roleHint;
    }

    /**
     * Provides a fully-constructed and injected instance of {@code T}.
     * 
     * @return instance of {@code T}.
     * @throws RuntimeException if the injector encounters an error while providing an instance. For example, if an
     *             injectable member on {@code T} throws an exception, the injector may wrap the exception and throw it
     *             to the caller of {@code get()}. Callers should not try to handle such exceptions as the behavior may
     *             vary across injector implementations and even different configurations of the same injector.
     */
    @SuppressWarnings("unchecked")
    public T get()
    {
        T component;

        try {
            Class<T> roleClass = this.roleHint.getRoleClass();

            if (roleClass.isAssignableFrom(Provider.class) || roleClass.isAssignableFrom(javax.inject.Provider.class)) {
                try {
                    component = this.componentManager.getInstance(this.roleHint.getRoleType(), this.roleHint.getHint());
                } catch (ComponentLookupException e) {
                    // Inject a default Provider
                    component = (T) newProvider(this.componentManager,
                        new RoleHint<>(ReflectionUtils.getLastTypeGenericArgument(this.roleHint.getRoleType()),
                            this.roleHint.getHint()));
                }
            } else if (roleClass.isAssignableFrom(List.class)) {
                component = (T) this.componentManager
                    .getInstanceList(ReflectionUtils.getLastTypeGenericArgument(this.roleHint.getRoleType()));
            } else if (roleClass.isAssignableFrom(Map.class)) {
                component = (T) this.componentManager
                    .getInstanceMap(ReflectionUtils.getLastTypeGenericArgument(this.roleHint.getRoleType()));
            } else if (ReflectionUtils.getDirectAnnotation(ComponentRole.class, roleClass) != null
                && ReflectionUtils.getDirectAnnotation(Role.class, roleClass) == null) {
                // since 4.0M1, retro-compatibility (generic type used to not be taken into account)
                component = getInstance(roleClass, this.roleHint.getHint());
            } else {
                component = getInstance(this.roleHint.getRoleType(), this.roleHint.getHint());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get [" + this.roleHint + "]", e);
        }

        return component;
    }

    protected abstract AbstractGenericProvider<T> newProvider(ComponentManager componentManager, RoleHint<T> roleHint);

    protected T getInstance(Type type, String hint) throws ComponentLookupException
    {
        return this.componentManager.getInstance(type, hint);
    }
}
