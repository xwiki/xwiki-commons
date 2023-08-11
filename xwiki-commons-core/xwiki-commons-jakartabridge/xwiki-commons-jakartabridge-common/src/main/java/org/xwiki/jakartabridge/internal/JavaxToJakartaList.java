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

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

import org.xwiki.jakartabridge.JakartaBridge;

/**
 * @param <X> the javax type
 * @param <K> the jakarta type
 * @version $Id$
 * @since 42.0.0
 */
public class JavaxToJakartaList<X, K> extends JavaxToJakartaCollection<List<K>, X, K> implements List<X>
{
    /**
     * @param wrapped the wrapped version
     * @param javaxProvider the javax wrapper provider
     * @param jakartaProvider the jakarta wrapper provider
     */
    public JavaxToJakartaList(List<K> wrapped, Function<K, X> javaxProvider, Function<X, K> jakartaProvider)
    {
        super(wrapped, javaxProvider, jakartaProvider);
    }

    @Override
    public boolean addAll(int index, Collection<? extends X> c)
    {
        return getJakarta().addAll(index, JakartaBridge.toJakarta(c, this.javaxProvider, this.jakartaProvider));
    }

    @Override
    public X get(int index)
    {
        return this.javaxProvider.apply(getJakarta().get(index));
    }

    @Override
    public X set(int index, X element)
    {
        return this.javaxProvider.apply(getJakarta().set(index, this.jakartaProvider.apply(element)));
    }

    @Override
    public void add(int index, X element)
    {
        getJakarta().add(index, this.jakartaProvider.apply(element));
    }

    @Override
    public X remove(int index)
    {
        return this.javaxProvider.apply(getJakarta().remove(index));
    }

    @Override
    public int indexOf(Object o)
    {
        return getJakarta().indexOf(this.jakartaProvider.apply((X) o));
    }

    @Override
    public int lastIndexOf(Object o)
    {
        return getJakarta().lastIndexOf(this.jakartaProvider.apply((X) o));
    }

    @Override
    public ListIterator<X> listIterator()
    {
        return JakartaBridge.toJavax(getJakarta().listIterator(), this.javaxProvider, this.jakartaProvider);
    }

    @Override
    public ListIterator<X> listIterator(int index)
    {
        return JakartaBridge.toJavax(getJakarta().listIterator(index), this.javaxProvider, this.jakartaProvider);
    }

    @Override
    public List<X> subList(int fromIndex, int toIndex)
    {
        return JakartaBridge.toJavax(getJakarta().subList(fromIndex, toIndex), this.javaxProvider,
            this.jakartaProvider);
    }
}
