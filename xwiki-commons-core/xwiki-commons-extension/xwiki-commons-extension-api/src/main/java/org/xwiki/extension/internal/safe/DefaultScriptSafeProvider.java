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
package org.xwiki.extension.internal.safe;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.component.util.ReflectionUtils;

/**
 * Find the right safe provider for the passed object.
 * 
 * @version $Id$
 * @since 4.0M2
 */
@Component
@Singleton
@SuppressWarnings("rawtypes")
public class DefaultScriptSafeProvider implements ScriptSafeProvider
{
    /**
     * Used to lookup {@link ScriptSafeProvider} implementations.
     */
    @Inject
    private ComponentManager component;

    /**
     * The logger.
     */
    @Inject
    private Logger logger;

    @Override
    public Object get(Object unsafe)
    {
        if (unsafe == null) {
            return null;
        }

        Object safe = get(unsafe, Arrays.<Type> asList(unsafe.getClass()));

        if (safe == null) {
            safe = unsafe;
        }

        return safe;
    }

    /**
     * @param unsafe the unsafe version of the object
     * @param types the types implemented or extended by the object for which to search a provider
     * @return a safe version of the passed object, null if none could be provided
     */
    private Object get(Object unsafe, List<Type> types)
    {
        for (Type type : types) {
            Object safe = get(unsafe, type);

            if (safe != null) {
                return safe;
            }
        }

        for (Type type : types) {
            Object safe = get(unsafe, ReflectionUtils.getDirectTypes(type));

            if (safe != null) {
                return safe;
            }
        }

        return null;
    }

    /**
     * @param unsafe the unsafe version of the object
     * @param type the type implemented or extended by the object for which to search a provider
     * @return a safe version of the passed object, null if none could be provided
     */
    private Object get(Object unsafe, Type type)
    {
        Type completeRole = new DefaultParameterizedType(null, ScriptSafeProvider.class, type);

        if (this.component.hasComponent(completeRole)) {
            try {
                ScriptSafeProvider<Object> provider = this.component.getInstance(completeRole);

                return provider.get(unsafe);
            } catch (ComponentLookupException e) {
                this.logger.error("Failed to load safe provider for complete type [{}]", type, e);
            }
        } else if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            Type rawRole = new DefaultParameterizedType(null, ScriptSafeProvider.class, rawType);

            this.logger.debug("Could not find any safe provider for type [{}]. Trying with [{}]", type, rawType);

            if (this.component.hasComponent(rawRole)) {
                try {
                    ScriptSafeProvider<Object> provider = this.component.getInstance(rawRole);

                    return provider.get(unsafe);
                } catch (ComponentLookupException e) {
                    this.logger.error("Failed to load safe provider for raw type [{}]", rawType, e);
                }
            }
        }

        return null;
    }
}
