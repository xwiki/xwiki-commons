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
package org.xwiki.jakartabridge.internal;

import org.xwiki.jakartabridge.JakartaToJavaxWrapper;

/**
 * @param <T> the type of the wrapped object
 * @version $Id$
 * @since 17.0.0RC1
 */
public abstract class AbstractJakartaToJavaxWrapper<T> implements JakartaToJavaxWrapper<T>
{
    protected final T javax;

    /**
     * @param wrapped the wrapped object
     */
    protected AbstractJakartaToJavaxWrapper(T wrapped)
    {
        this.javax = wrapped;
    }

    @Override
    public T getJavax()
    {
        return this.javax;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof JakartaToJavaxWrapper<?> otherWrapper) {
            return this.javax.equals(otherWrapper.getJavax());
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        return this.javax.hashCode();
    }
}
