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
package org.xwiki.velocity.tools;

import org.apache.commons.lang3.ObjectUtils;

/**
 * Miscellaneous methods around objects, including checks for null.
 *
 * @version $Id$
 * @since 12.1
 */
public class ObjectTool extends ObjectUtils
{
    /**
     * It's not always easy with Velocity to check for null. You can use some tricks such as
     * {@code #if ($variable == $NULL)} or {@code #if (!$variable)}. However Velocity performs
     * some conversions, calling methods such as getAsBoolean() or getAsString(). There are
     * cases when you don't want them to be called as they can clash with your own implementation (note: this is the
     * case for JsonObject from gson for ex). If you need to check only for null, this method
     * is probably the best for that.
     *
     * @param object the object to test for null
     * @return true if the object is null or false otherwise
     */
    public boolean isNull(Object object)
    {
        return object == null;
    }

    /**
     * @param object the object to test for not null
     * @return true if the object is not null or false otherwise
     * @see #isNull(Object)
     */
    public boolean isNotNull(Object object)
    {
        return !isNull(object);
    }

    /**
     * Convenience method since Velocity doesn't have a way to create a null object.
     *
     * @return a null object
     */
    public Object getNull()
    {
        return null;
    }
}
