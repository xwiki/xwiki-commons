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

import java.util.Iterator;
import java.util.function.Function;

/**
 * @param <I> the type of jakarta {@link Iterator}
 * @param <X> the javax type
 * @param <K> the jakarta type
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JavaxToJakartaIterator<I extends Iterator<K>, X, K> extends AbstractJavaxToJakartaWrapper<I>
    implements Iterator<X>
{
    protected final Function<K, X> javaxProvider;

    /**
     * @param wrapped the wrapped version
     * @param javaxProvider the javax wrapper provider
     */
    public JavaxToJakartaIterator(I wrapped, Function<K, X> javaxProvider)
    {
        super(wrapped);

        this.javaxProvider = javaxProvider;
    }

    @Override
    public boolean hasNext()
    {
        return getJakarta().hasNext();
    }

    @Override
    public X next()
    {
        return this.javaxProvider.apply(getJakarta().next());
    }

}
