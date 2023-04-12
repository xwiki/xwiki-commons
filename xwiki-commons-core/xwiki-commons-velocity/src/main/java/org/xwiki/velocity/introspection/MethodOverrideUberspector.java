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
package org.xwiki.velocity.introspection;

import java.lang.reflect.Method;

import org.apache.velocity.util.introspection.AbstractChainableUberspector;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.VelMethod;
import org.xwiki.collection.SoftCache;

/**
 * Workaround for https://issues.apache.org/jira/browse/VELOCITY-952: call the lowest level method overridden by the one
 * found by others uberspectors.
 * 
 * @version $Id$
 * @since 15.4RC1
 */
public class MethodOverrideUberspector extends AbstractChainableUberspector
{
    private final SoftCache<Method, VelMethod> cache = new SoftCache<>();

    @Override
    public VelMethod getMethod(Object obj, String methodName, Object[] args, Info i)
    {
        // Let Velocity find a matching method. However, Velocity finds Method according to obj's Class and not to the
        // return Type of the method which provided obj in the first place.
        // Return the Method instance associated to the lowest Class/interface overwritten by the one found by Velocity
        // to lower the risk of IllegalAccessException.

        VelMethod initialVelMethod = super.getMethod(obj, methodName, args, i);
        VelMethod velMethod = initialVelMethod;

        if (velMethod != null) {
            Method method = velMethod.getMethod();

            // Make sure we are not in a case where the method does not match the object (like with arrays manipulated
            // as List)
            // If the method is accessible, there is nothing to do
            if (method.getDeclaringClass().isInstance(obj) && !method.canAccess(obj)) {
                velMethod = getRootMethod(method, obj);
            }
        }

        return velMethod;
    }

    private VelMethod getRootMethod(Method method, Object obj)
    {
        VelMethod velMethod = this.cache.get(method);

        if (velMethod == null) {
            // Search in super classes
            Method rootMethod = getRootSuperMethod(method, obj);

            // Search in interfaces
            rootMethod = getRootInterfacesMethod(rootMethod, obj);

            if (rootMethod != method) {
                velMethod = new VelMethodImpl(rootMethod);
                this.cache.put(method, velMethod);
            }
        }

        return velMethod;
    }

    private Method getRootInterfacesMethod(Method method, Object obj)
    {
        Method rootMethod = method;
        Class<?> methodClass = rootMethod.getDeclaringClass();
        for (Class<?> iface : methodClass.getInterfaces()) {
            try {
                rootMethod = iface.getMethod(method.getName(), method.getParameterTypes());

                // The new method interface is accessible, no need to search another one
                if (!rootMethod.canAccess(obj)) {
                    // Search in super interfaces
                    rootMethod = getRootInterfacesMethod(rootMethod, obj);
                }
            } catch (Exception e) {
                continue;
            }

            // If an overridden method was found down one of the interfaces branches, no need to try the others
            if (rootMethod != method) {
                return rootMethod;
            }
        }

        return rootMethod;
    }

    private Method getRootSuperMethod(Method method, Object obj)
    {
        Class<?> methodClass;
        Method rootMethod = method;
        for (Class<?> superClass = method.getDeclaringClass().getSuperclass(); !rootMethod.canAccess(obj); superClass =
            methodClass.getSuperclass()) {
            try {
                rootMethod = superClass.getMethod(method.getName(), method.getParameterTypes());
            } catch (Exception e) {
                break;
            }
            methodClass = rootMethod.getDeclaringClass();
        }

        return rootMethod;
    }
}
