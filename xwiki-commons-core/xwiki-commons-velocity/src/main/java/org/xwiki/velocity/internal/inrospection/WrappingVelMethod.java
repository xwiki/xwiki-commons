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
import java.lang.reflect.Method;

import org.apache.velocity.util.introspection.VelMethod;

/**
 * Generic wrapping {@link VelMethod} implementation.
 *
 * @version $Id$
 * @since 12.4RC1
 */

public class WrappingVelMethod implements VelMethod
{
    /**
     * The real method that performs the actual call.
     */
    private VelMethod innerMethod;

    /**
     * Constructor.
     *
     * @param realMethod the real method to wrap
     */
    public WrappingVelMethod(VelMethod realMethod)
    {
        this.innerMethod = realMethod;
    }

    /**
     * @return the wrapped {@link VelMethod}
     */
    public VelMethod getWrappedVelMethod()
    {
        return this.innerMethod;
    }

    @Override
    public Object invoke(Object o, Object[] params) throws IllegalAccessException, InvocationTargetException
    {
        return this.innerMethod.invoke(o, params);
    }

    @Override
    public boolean isCacheable()
    {
        return this.innerMethod.isCacheable();
    }

    @Override
    public String getMethodName()
    {
        return this.innerMethod.getMethodName();
    }

    @Override
    public Class<?> getReturnType()
    {
        return this.innerMethod.getReturnType();
    }

    @Override
    public Method getMethod()
    {
        return this.innerMethod.getMethod();
    }
}
