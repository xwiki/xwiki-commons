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

import java.util.ListIterator;
import java.util.function.Function;

/**
 * @param <X> the javax type
 * @param <K> the jakarta type
 * @version $Id$
 */
public class JavaxToJakartaListIterator<X, K> extends JavaxToJakartaIterator<ListIterator<K>, X, K>
    implements ListIterator<X>
{
    protected final Function<X, K> jakartaConstructor;

    /**
     * @param wrapped the wrapped version
     * @param javaxProvider the javax wrapper provider
     * @param jakartaProvider the jakarta wrapper provider
     */
    public JavaxToJakartaListIterator(ListIterator<K> wrapped, Function<K, X> javaxProvider,
        Function<X, K> jakartaProvider)
    {
        super(wrapped, javaxProvider);

        this.jakartaConstructor = jakartaProvider;
    }

    @Override
    public boolean hasPrevious()
    {
        return getJakarta().hasPrevious();
    }

    @Override
    public X previous()
    {
        return this.javaxProvider.apply(getJakarta().previous());
    }

    @Override
    public int nextIndex()
    {
        return getJakarta().nextIndex();
    }

    @Override
    public int previousIndex()
    {
        return getJakarta().previousIndex();
    }

    @Override
    public void remove()
    {
        getJakarta().remove();
    }

    @Override
    public void set(X e)
    {
        getJakarta().set(this.jakartaConstructor.apply(e));
    }

    @Override
    public void add(X e)
    {
        getJakarta().add(this.jakartaConstructor.apply(e));
    }
}
