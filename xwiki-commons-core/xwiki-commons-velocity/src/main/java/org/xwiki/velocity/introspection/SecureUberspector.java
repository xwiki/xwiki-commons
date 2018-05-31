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

import java.util.Iterator;

import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.util.RuntimeServicesAware;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.SecureIntrospectorControl;
import org.apache.velocity.util.introspection.UberspectImpl;

/**
 * {@link org.apache.velocity.util.introspection.SecureUberspector} is way too restrictive regarding {@link Class}
 * methods allowed.
 *
 * @version $Id$
 * @since 5.4RC1
 */
public class SecureUberspector extends UberspectImpl implements RuntimeServicesAware
{
    @Override
    public void init()
    {
        String[] badPackages =
            this.rsvc.getConfiguration().getStringArray(RuntimeConstants.INTROSPECTOR_RESTRICT_PACKAGES);

        String[] badClasses =
            this.rsvc.getConfiguration().getStringArray(RuntimeConstants.INTROSPECTOR_RESTRICT_CLASSES);

        this.introspector = new SecureIntrospector(badClasses, badPackages, this.log);
    }

    /**
     * Get an iterator from the given object. Since the superclass method this secure version checks for execute
     * permission.
     *
     * @param obj object to iterate over
     * @param i line, column, template info
     * @return Iterator for object
     */
    @Override
    public Iterator getIterator(Object obj, Info i)
    {
        if (obj != null) {
            SecureIntrospectorControl sic = (SecureIntrospectorControl) this.introspector;
            if (sic.checkObjectExecutePermission(obj.getClass(), null)) {
                return super.getIterator(obj, i);
            } else {
                this.log.warn("Cannot retrieve iterator from [{}] due to security restrictions.", obj.getClass());
            }
        }

        return null;
    }
}
