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

import javax.inject.Provider;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;

/**
 * Wrapper used to expose a {@link Provider} as a {@link javax.inject.Provider}.
 *
 * @param <T> the role type to provide
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JakartaJavaxProvider<T> implements Provider<T>
{
    private final ComponentManager componentManager;

    private final Type providerType;

    private final String roleHint;

    /**
     * @param componentManager the Component Manager instance to use to get the wrapped {@link jakarta.inject.Provider}
     * @param providerType the role type to provide
     * @param roleHint the hint that differentiates a component implementation from another one (each component is
     *            registered with a hint; the "default" hint being the default)
     */
    public JakartaJavaxProvider(ComponentManager componentManager, Type providerType, String roleHint)
    {
        this.componentManager = componentManager;
        this.providerType = providerType;
        this.roleHint = roleHint;
    }

    @Override
    public T get()
    {
        jakarta.inject.Provider<T> provider;
        try {
            provider = this.componentManager.getInstance(this.providerType, this.roleHint);
        } catch (ComponentLookupException e) {
            throw new RuntimeException(
                "Failed to get the provider [" + this.providerType + "] with role hint [" + this.roleHint + "]", e);
        }

        return provider.get();
    }
}
