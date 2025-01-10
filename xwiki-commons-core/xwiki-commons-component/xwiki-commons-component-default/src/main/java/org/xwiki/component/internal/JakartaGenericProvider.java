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

import org.xwiki.component.manager.ComponentManager;

import jakarta.inject.Provider;

/**
 * Default provider used when the Component Manager needs to inject a {@link Provider} field but no custom Provider has
 * been registered. The default behavior is simply to look up the Component role class when {@link Provider#get()} is
 * called. This is useful for example when you wish to do "lazy injection".
 *
 * @param <T> the role type
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JakartaGenericProvider<T> extends AbstractGenericProvider<T> implements Provider<T>
{
    /**
     * @param componentManager the Component Manager instance that we'll use to look up the Component Role during
     *            {@link javax.inject.Provider#get()}
     * @param roleHint the Component Role and Hint that uniquely identify the Component we wish to provide for
     */
    public JakartaGenericProvider(ComponentManager componentManager, RoleHint<T> roleHint)
    {
        super(componentManager, roleHint);
    }

    @Override
    protected AbstractGenericProvider<T> newProvider(ComponentManager componentManager, RoleHint roleHint)
    {
        return new JakartaGenericProvider<>(componentManager, roleHint);
    }
}
