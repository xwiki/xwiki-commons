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
package org.xwiki.netflux.internal.user;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.inject.Inject;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.netflux.internal.NetfluxException;
import org.xwiki.netflux.internal.User;

/**
 * Automatically use the right {@link UserHandler} based on passed {@link User} class.
 * 
 * @version $Id$
 * @since 17.10.0RC1
 */
@SuppressWarnings("rawtypes")
public class DefaultUserHandler implements UserHandler
{
    @Inject
    private ComponentManager componentManager;

    private final Map<Class<?>, UserHandler<?>> handlers = new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked"})
    @Override
    public void sendText(User user, String text) throws NetfluxException
    {
        UserHandler handler = this.handlers.get(user.getClass());

        if (handler == null) {
            try {
                handler = this.componentManager
                    .getInstance(new DefaultParameterizedType(null, UserHandler.class, user.getClass()));
            } catch (ComponentLookupException e) {
                throw new NoUserHandlerException(
                    "Failed to find a UserHandler for type [%s]".formatted(user.getClass()), e);
            }
        }

        handler.sendText(user, text);
    }
}
