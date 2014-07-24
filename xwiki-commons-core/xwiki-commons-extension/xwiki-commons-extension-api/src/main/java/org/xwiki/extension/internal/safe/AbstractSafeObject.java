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

import org.xwiki.extension.wrap.AbstractWrappingObject;

/**
 * Encapsulate an object in a safe way for public scripts.
 *
 * @param <T> the type of the wrapped object
 * @version $Id$
 * @since 4.0M2
 */
public abstract class AbstractSafeObject<T> extends AbstractWrappingObject<T>
{
    /**
     * The message used in forbidden access exceptions.
     */
    public static final String FORBIDDEN = "Operation forbidden in script proxy";

    /**
     * The provider of instances safe for public scripts.
     */
    protected ScriptSafeProvider<Object> safeProvider;

    /**
     * @param wrapped the wrapped object
     * @param safeProvider the provider of instances safe for public scripts
     */
    public AbstractSafeObject(T wrapped, ScriptSafeProvider<?> safeProvider)
    {
        super(wrapped);

        this.safeProvider = (ScriptSafeProvider<Object>) safeProvider;
    }

    /**
     * @param <S> the type of the object
     * @param unsafe the unsafe object
     * @return the safe version of the object
     */
    protected <S> S safe(Object unsafe)
    {
        return this.safeProvider.get(unsafe);
    }
}
