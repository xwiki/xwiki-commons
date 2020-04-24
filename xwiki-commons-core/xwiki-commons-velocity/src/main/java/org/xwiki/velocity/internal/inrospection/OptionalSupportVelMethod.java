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
package org.xwiki.velocity.internal.inrospection;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import org.apache.velocity.util.introspection.VelMethod;

/**
 * Wraps a {@link VelMethod} in order handle {@link Optional} by unwrapping them to make them simpler and more
 * natural to use from Velocity: if the Optional contains a non-null value returns it and if not returns null.
 *
 * @version $Id$
 * @since 12.4RC1
 */
public class OptionalSupportVelMethod extends WrappingVelMethod
{
    /**
     * @param wrappedMethod the Vel method to wrap
     */
    public OptionalSupportVelMethod(VelMethod wrappedMethod)
    {
        super(wrappedMethod);
    }

    @Override
    public Object invoke(Object o, Object[] params) throws IllegalAccessException, InvocationTargetException
    {
        Object result = getWrappedVelMethod().invoke(o, params);
        if (result instanceof Optional) {
            Optional optional = (Optional) result;
            if (optional.isPresent()) {
                result = optional.get();
            } else {
                result = null;
            }
        }
        return result;
    }
}
