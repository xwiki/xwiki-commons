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

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.util.introspection.SecureIntrospectorImpl;

/**
 * {@link SecureIntrospectorImpl} is way too restrictive with allowed {@link Class} methods.
 * 
 * @version $Id$
 * @since 5.4RC1
 */
public class SecureIntrospector extends SecureIntrospectorImpl
{
    private final Set<String> secureClassMethods = new HashSet<String>();

    /**
     * @param badClasses forbidden classes
     * @param badPackages forbidden packages
     * @param log the log
     */
    public SecureIntrospector(String[] badClasses, String[] badPackages, Log log)
    {
        super(badClasses, badPackages, log);

        this.secureClassMethods.add("getName");
        this.secureClassMethods.add("name");

        this.secureClassMethods.add("isArray");
        this.secureClassMethods.add("isAssignableFrom");
        this.secureClassMethods.add("isEnum");
        this.secureClassMethods.add("isInstance");
        this.secureClassMethods.add("isInterface");
        this.secureClassMethods.add("isLocalClass");
        this.secureClassMethods.add("isMemberClass");
        this.secureClassMethods.add("isPrimitive");
        this.secureClassMethods.add("isSynthetic");

        // TODO: add more when needed
    }

    @Override
    public boolean checkObjectExecutePermission(Class clazz, String methodName)
    {
        if (Class.class.isAssignableFrom(clazz) && methodName != null && this.secureClassMethods.contains(methodName)) {
            return true;
        } else {
            return super.checkObjectExecutePermission(clazz, methodName);
        }
    }
}
