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
package org.xwiki.test.mockito;

import static org.mockito.Mockito.mock;

import java.lang.reflect.Type;

import org.xwiki.component.embed.GenericProvider;
import org.xwiki.component.internal.RoleHint;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.ReflectionUtils;

/**
 * Override {@link GenericProvider} to fallback on a mock when the target component is not found.
 * 
 * @param <T> the role type
 * @version $Id$
 * @since 7.0M1
 */
public class GenericMockerProvider<T> extends GenericProvider<T>
{
    /**
     * @param componentManager the Component Manager instance that we'll use to look up the Component Role during
     *            {@link javax.inject.Provider#get()}
     * @param roleHint the Component Role and Hint that uniquely identify the Component we wish to provide for
     */
    public GenericMockerProvider(ComponentManager componentManager, RoleHint<T> roleHint)
    {
        super(componentManager, roleHint);
    }

    @Override
    protected T getInstance(Type type, String hint) throws ComponentLookupException
    {
        if (this.componentManager.hasComponent(type, hint)) {
            return super.getInstance(type, hint);
        } else {
            return (T) mock(ReflectionUtils.getTypeClass(type));
        }
    }
}
